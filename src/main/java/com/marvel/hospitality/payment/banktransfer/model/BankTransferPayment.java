package com.marvel.hospitality.payment.banktransfer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table
@Data
@NoArgsConstructor // for JPA
public class BankTransferPayment {
@Id
@Column(length = 64 ,  nullable = false, updatable = false)
 private String paymentId;
    @Column(length =8 ,  nullable = false, updatable = false)
    private String reservationId;

    @Column(length =10 ,  nullable = false, updatable = false)
    private String etoEUniqueId;

    @Column(length =64 ,  nullable = false, updatable = false)
    private String debtorAccountnumber;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amountReceived;

    @Column(length= 18 ,  nullable = false, updatable = false)
    private String transactionDescription;

    @Column(length= 18 ,  nullable = false, updatable = false)
    private Instant recivedAt;

   /**
    * @param paymentId  payment id from the topic
    * @param reservationId reservation id from the reservation
    * @param etoEUniqueId unique end to end id
    * @param debtorAccountnumber transaction account number
    * @param amountReceived amout received
    * @param transactionDescription E2E unique id(10 character) + reservationId (8 characters)
    * @param recivedAt time of insertion
    */
   public BankTransferPayment(String paymentId,
                              String reservationId,
                              String etoEUniqueId,
                              String debtorAccountnumber,
                              BigDecimal amountReceived,
                              String transactionDescription,
                              Instant recivedAt) {
      this.paymentId = paymentId;
      this.reservationId = reservationId;
      this.etoEUniqueId = etoEUniqueId;
      this.debtorAccountnumber = debtorAccountnumber;
      this.amountReceived = amountReceived;
      this.transactionDescription = transactionDescription;
      this.recivedAt = recivedAt;
   }
}
