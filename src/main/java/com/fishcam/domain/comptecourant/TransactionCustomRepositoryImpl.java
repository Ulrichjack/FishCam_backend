package com.fishcam.domain.comptecourant;

import com.fishcam.adapter.web.dto.response.TransactionGlobalResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TransactionCustomRepositoryImpl implements TransactionCustomRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<TransactionGlobalResponse> findAllTransactionsDynamically(
            Long poissonnerieId, String type, String searchTerm, LocalDate date, Pageable pageable) {

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ( ");

        // PARTIE 1 : Transactions Compte Courant
        sql.append("SELECT 'CC_' || tcc.id as id, tcc.transaction_date as date_heure, ");
        sql.append("c.first_name || ' ' || c.last_name as client_nom, c.phone as client_telephone, ");
        sql.append("tcc.type as type, tcc.montant as montant ");
        sql.append("FROM transaction_compte_courant tcc ");
        sql.append("JOIN compte_courant cc ON tcc.compte_courant_id = cc.id ");
        sql.append("JOIN client c ON cc.client_id = c.id ");
        sql.append("WHERE tcc.poissonnerie_id = :poissonnerieId ");

        sql.append("UNION ALL ");

        // PARTIE 2 : Transactions Epargne
        sql.append("SELECT 'EP_' || te.id as id, te.transaction_date as date_heure, ");
        sql.append("c.first_name || ' ' || c.last_name as client_nom, c.phone as client_telephone, ");
        sql.append("te.type as type, te.amount as montant ");
        sql.append("FROM transaction_saving te ");
        sql.append("JOIN epargne_saving e ON te.saving_id = e.id ");

        sql.append("JOIN client c ON e.client_id = c.id ");
        sql.append("WHERE te.poissonnerie_id = :poissonnerieId ");

        sql.append(") as combined WHERE 1=1 ");

        if (type != null && !type.isBlank()) {
            sql.append("AND combined.type = :type ");
        }
        if (searchTerm != null && !searchTerm.isBlank()) {
            sql.append("AND (LOWER(combined.client_nom) LIKE LOWER(:search) OR combined.client_telephone LIKE :search) ");
        }
        if (date != null) {
            sql.append("AND DATE(combined.date_heure) = :date ");
        }

        String countSql = "SELECT COUNT(*) FROM (" + sql.toString() + ") as count_table";
        Query countQuery = entityManager.createNativeQuery(countSql);

        // 2. ENSUITE on ajoute l'ORDER BY pour la vraie requête
        sql.append("ORDER BY combined.date_heure DESC ");
        Query query = entityManager.createNativeQuery(sql.toString());

        query.setParameter("poissonnerieId", poissonnerieId);
        countQuery.setParameter("poissonnerieId", poissonnerieId);

        if (type != null && !type.isBlank()) {
            query.setParameter("type", type);
            countQuery.setParameter("type", type);
        }
        if (searchTerm != null && !searchTerm.isBlank()) {
            query.setParameter("search", "%" + searchTerm + "%");
            countQuery.setParameter("search", "%" + searchTerm + "%");
        }
        if (date != null) {
            query.setParameter("date", date);
            countQuery.setParameter("date", date);
        }

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        long total = ((Number) countQuery.getSingleResult()).longValue();

        List<Object[]> results = query.getResultList();
        List<TransactionGlobalResponse> content = new ArrayList<>();

        for (Object[] row : results) {
            content.add(TransactionGlobalResponse.builder()
                    .id((String) row[0])
                    .dateHeure(((java.sql.Timestamp) row[1]).toLocalDateTime())
                    .clientNom((String) row[2])
                    .clientTelephone((String) row[3])
                    .type((String) row[4])
                    .montant((BigDecimal) row[5])
                    .build());
        }

        return new PageImpl<>(content, pageable, total);
    }
}