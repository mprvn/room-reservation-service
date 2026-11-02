package com.marvel.hospitality.payment.banktransfer.repository;

import com.marvel.hospitality.payment.banktransfer.model.BankTransferPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankTransferPaymentRepository extends JpaRepository<BankTransferPayment, String> {
}
