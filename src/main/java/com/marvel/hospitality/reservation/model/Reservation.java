package com.marvel.hospitality.reservation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table
@Data
@AllArgsConstructor
public class Reservation{
    @Id
    @Column(length = 8, nullable = false, updatable = false)
    private String id;

    @Column( nullable = false, length = 200)
    private String customerName;

    @Column( nullable = false, length = 20)
    private String roomNumber;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column( nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column( nullable = false, length = 20)
    private RoomSegment roomSegment;


    @Column(length = 100)
    private String paymentReference;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.VARCHAR)
  @Column(nullable = false, length = 20)
  private PaymentMode paymentMode;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column( nullable = false, length = 20)
    private ReservationStatus status;

    @Column( nullable = false, precision = 12, scale = 2)
    private BigDecimal amountPaid;

    @Column( nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false, updatable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected Reservation() {
        // for JPA
    }

  public Reservation(String id, String customerName, String roomNumber, LocalDate startDate, LocalDate endDate, RoomSegment roomSegment, PaymentMode paymentMode, String paymentReference, BigDecimal totalAmount) {
           this.id = id;
           this.customerName = customerName;
           this. roomNumber = roomNumber;
           this.startDate = startDate;
           this.endDate = endDate;
          this.roomSegment = roomSegment;
          this.paymentMode = paymentMode;
          this.paymentReference = paymentReference;
          this.totalAmount = totalAmount;
          this.amountPaid = BigDecimal.ZERO;
          this.status = ReservationStatus.PENDING_PAYMENT;
    }


  public static Reservation create(String id, String customerName, String roomNumber, LocalDate startDate,
                                   LocalDate endDate, RoomSegment roomSegment, PaymentMode paymentMode,
                                   String paymentReference, BigDecimal totalAmount) {
    if (!endDate.isAfter(startDate)) {
      throw new IllegalArgumentException("endDate must be after startDate");
    }
    return new Reservation(id, customerName, roomNumber, startDate, endDate, roomSegment, paymentMode,
            paymentReference, totalAmount);
  }

  public void confirm() {
    this.status = ReservationStatus.CONFIRMED;
  }
  public boolean isFullyPaid() {

        return amountPaid.compareTo(totalAmount) >= 0;
    }

  public void cancel() {
    this.status = ReservationStatus.CANCELLED;
  }
  public void  registerAmount(BigDecimal amountRecived) {
        if( amountRecived == null || amountRecived.signum() <= -1 ){
            throw new IllegalArgumentException("Amount received should be positive");
        }
        this.amountPaid = this.amountPaid.add(amountRecived);
  }

  @PrePersist
  void onCreate() {
    Instant now = Instant.now();
    this.createdAt = now;
    this.updatedAt = now;
  }
  @PreUpdate
  void onUpdate() {
    this.updatedAt = Instant.now();
  }
}
