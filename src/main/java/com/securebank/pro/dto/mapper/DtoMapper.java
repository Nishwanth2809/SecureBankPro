package com.securebank.pro.dto.mapper;

import org.springframework.stereotype.Component;
import com.securebank.pro.entity.User;
import com.securebank.pro.entity.Account;
import com.securebank.pro.entity.Transaction;
import com.securebank.pro.dto.request.RegisterRequestDTO;
import com.securebank.pro.dto.request.LoginRequestDTO;
import com.securebank.pro.dto.response.AccountResponseDTO;
import com.securebank.pro.dto.response.TransactionResponseDTO;

@Component
public class DtoMapper {

    /**
     * Converts a RegisterRequestDTO to a User entity.
     */
    public User convertDTOToEntity(RegisterRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        return new User(dto.getFullName(), dto.getEmail(), dto.getPassword());
    }

    /**
     * Converts a LoginRequestDTO to a User entity (for verification context).
     */
    public User convertDTOToEntity(LoginRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        return new User(null, dto.getEmail(), dto.getPassword());
    }

    /**
     * Converts an Account entity to an AccountResponseDTO.
     */
    public AccountResponseDTO convertEntityToDTO(Account account) {
        if (account == null) {
            return null;
        }
        String ownerName = account.getOwner() != null ? account.getOwner().getFullName() : "N/A";
        return new AccountResponseDTO(
            account.getAccountNumber(),
            account.getBalance(),
            account.getAccountType() != null ? account.getAccountType().name() : "N/A",
            ownerName
        );
    }

    /**
     * Converts a Transaction entity to a TransactionResponseDTO.
     */
    public TransactionResponseDTO convertEntityToDTO(Transaction transaction) {
        if (transaction == null) {
            return null;
        }
        return new TransactionResponseDTO(
            transaction.getReferenceNumber(),
            transaction.getTransactionType() != null ? transaction.getTransactionType().name() : "N/A",
            transaction.getAmount(),
            transaction.getCreatedAt()
        );
    }
}
