package com.tattos.repository;

import com.tattos.model.Appointment;
import com.tattos.model.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByDateOrderByStartTimeAsc(LocalDate date);

    List<Appointment> findByArtistIdOrderByDateDescStartTimeAsc(Long artistId);

    List<Appointment> findByStatusOrderByDateDescStartTimeAsc(AppointmentStatus status);

    List<Appointment> findByDateBetweenOrderByDateAscStartTimeAsc(LocalDate start, LocalDate end);

    @Query("SELECT a FROM Appointment a WHERE a.artist.id = :artistId AND a.date = :date " +
           "AND a.status <> 'CANCELADO' " +
           "AND (:excludeId IS NULL OR a.id <> :excludeId) " +
           "AND (a.startTime < :endTime AND a.endTime > :startTime)")
    List<Appointment> findConflictingArtistAppointments(
        @Param("artistId") Long artistId,
        @Param("date") LocalDate date,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime,
        @Param("excludeId") Long excludeId
    );

    @Query("SELECT a FROM Appointment a WHERE LOWER(a.station) = LOWER(:station) AND a.date = :date " +
           "AND a.status <> 'CANCELADO' " +
           "AND (:excludeId IS NULL OR a.id <> :excludeId) " +
           "AND (a.startTime < :endTime AND a.endTime > :startTime)")
    List<Appointment> findConflictingStationAppointments(
        @Param("station") String station,
        @Param("date") LocalDate date,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime,
        @Param("excludeId") Long excludeId
    );
}
