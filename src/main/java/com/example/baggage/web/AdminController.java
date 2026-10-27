package com.example.baggage.web;

import com.example.baggage.model.BaggageRecord;
import com.example.baggage.model.BaggageStatus;
import com.example.baggage.model.RecordOrigin;
import com.example.baggage.model.RecoveryStatus;
import com.example.baggage.model.VerifiableAttribute;
import com.example.baggage.service.AdminForm;
import com.example.baggage.service.AuthorizedBaggageSource;
import com.example.baggage.service.BaggageAdminService;
import com.example.baggage.service.BaggageImportService;
import com.example.baggage.service.BaggageSearchService;
import com.example.baggage.service.BaggageView;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@Controller
public class AdminController {

    private final BaggageAdminService admin;
    private final BaggageImportService importer;
    private final BaggageSearchService search;
    private final List<AuthorizedBaggageSource> sources;

    public AdminController(BaggageAdminService admin, BaggageImportService importer, BaggageSearchService search,
                           ObjectProvider<AuthorizedBaggageSource> sources) {
        this.admin = admin;
        this.importer = importer;
        this.search = search;
        this.sources = sources.orderedStream().toList();
    }

    @GetMapping("/admin")
    public String dashboard(Model model) {
        List<BaggageView> views = admin.all().stream().map(search::toView).toList();
        model.addAttribute("records", views);
        model.addAttribute("dataMode", search.dataMode().name());
        model.addAttribute("sourceNames", sources.stream().map(AuthorizedBaggageSource::sourceName).toList());
        return "admin/list";
    }

    @GetMapping("/admin/new")
    public String newForm(Model model) {
        AdminForm form = new AdminForm();
        form.setDateLastChecked(LocalDate.now());
        return formPage(model, form);
    }

    @GetMapping("/admin/{id}/edit")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return admin.find(id)
                .map(r -> formPage(model, AdminForm.from(r)))
                .orElseGet(() -> {
                    ra.addFlashAttribute("error", "Record not found.");
                    return "redirect:/admin";
                });
    }

    @PostMapping("/admin/save")
    public String save(@ModelAttribute("form") AdminForm form, BindingResult binding, Model model,
                       Authentication auth, RedirectAttributes ra) {
        if (binding.hasErrors()) {
            binding.reject("invalid", "Some values could not be read - check the date format.");
            return formPage(model, form);
        }
        List<String> problems = admin.validate(form);
        if (!problems.isEmpty()) {
            problems.forEach(p -> binding.reject("invalid", p));
            return formPage(model, form);
        }
        BaggageRecord saved = admin.save(form, auth.getName());
        ra.addFlashAttribute("message", "Saved record " + (saved.getTagId() != null ? saved.getTagId() : saved.getCaseReference()) + ".");
        return "redirect:/admin";
    }

    @PostMapping("/admin/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        admin.delete(id);
        ra.addFlashAttribute("message", "Record deleted.");
        return "redirect:/admin";
    }

    @PostMapping("/admin/demo/delete")
    public String deleteDemo(RedirectAttributes ra) {
        int n = admin.deleteDemoRecords();
        ra.addFlashAttribute("message", "Removed " + n + " demonstration record(s).");
        return "redirect:/admin";
    }

    @GetMapping("/admin/import")
    public String importForm(Model model) {
        model.addAttribute("columns", String.join(", ", BaggageImportService.CSV_COLUMNS));
        model.addAttribute("today", LocalDate.now());
        return "admin/import";
    }

    @PostMapping("/admin/import")
    public String doImport(@RequestParam("file") MultipartFile file,
                           @RequestParam("source") String source,
                           @RequestParam(value = "authorized", defaultValue = "false") boolean authorized,
                           @RequestParam(value = "synthetic", defaultValue = "false") boolean synthetic,
                           @RequestParam("checkedOn") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkedOn,
                           Model model) throws IOException {
        model.addAttribute("columns", String.join(", ", BaggageImportService.CSV_COLUMNS));
        model.addAttribute("today", LocalDate.now());
        if (!authorized && !synthetic) {
            model.addAttribute("error", "Tick one box: either this is synthetic test data, or you are permitted to use this data.");
            return "admin/import";
        }
        if (file.isEmpty()) {
            model.addAttribute("error", "Choose a CSV file.");
            return "admin/import";
        }
        if (checkedOn.isAfter(LocalDate.now())) {
            model.addAttribute("error", "The checked-on date cannot be in the future.");
            return "admin/import";
        }
        String text = new String(file.getBytes(), StandardCharsets.UTF_8);
        model.addAttribute("result", importer.importCsv(text, source, checkedOn,
                synthetic ? RecordOrigin.DEMO : RecordOrigin.AUTHORIZED_EXPORT));
        return "admin/import";
    }

    @PostMapping("/admin/sync")
    public String syncSource(@RequestParam("name") String name, RedirectAttributes ra) {
        AuthorizedBaggageSource src = sources.stream().filter(s -> s.sourceName().equals(name)).findFirst().orElse(null);
        if (src == null) {
            ra.addFlashAttribute("error", "Unknown source.");
        } else {
            BaggageImportService.ImportResult r = importer.importFromSource(src);
            ra.addFlashAttribute("message", "Imported " + r.imported() + ", skipped " + r.skippedDuplicates()
                    + " duplicate(s), " + r.errors().size() + " error(s).");
        }
        return "redirect:/admin";
    }

    private String formPage(Model model, AdminForm form) {
        model.addAttribute("form", form);
        model.addAttribute("statuses", BaggageStatus.values());
        model.addAttribute("recoveries", RecoveryStatus.values());
        model.addAttribute("attributes", VerifiableAttribute.values());
        model.addAttribute("origins", List.of(RecordOrigin.MANUAL_ENTRY, RecordOrigin.AUTHORIZED_EXPORT));
        model.addAttribute("isDemo", form.getRecordOrigin() == RecordOrigin.DEMO);
        return "admin/form";
    }
}
