package com.example.baggage.repo;

import com.example.baggage.model.BaggageRecord;
import com.example.baggage.model.RecordOrigin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BaggageRepository extends JpaRepository<BaggageRecord, Long>, JpaSpecificationExecutor<BaggageRecord> {

    Optional<BaggageRecord> findFirstByTagId(String tagId);

    Optional<BaggageRecord> findFirstByCaseReference(String caseReference);

    long countByRecordOrigin(RecordOrigin origin);

    @Modifying
    @Query("delete from BaggageRecord r where r.recordOrigin = com.example.baggage.model.RecordOrigin.DEMO")
    int deleteDemoRecords();

    @Query("select distinct r.originAirport from BaggageRecord r where r.originAirport is not null")
    List<String> distinctOrigins();

    @Query("select distinct r.destinationAirport from BaggageRecord r where r.destinationAirport is not null")
    List<String> distinctDestinations();

    @Query("select distinct r.lastRecordedAirport from BaggageRecord r where r.lastRecordedAirport is not null")
    List<String> distinctLastAirports();
}
