ALTER TABLE transaction_compte_courant
    ADD COLUMN date_dette_origine DATE;

ALTER TABLE transaction_compte_courant
    DROP CONSTRAINT IF EXISTS transaction_compte_courant_type_check;

ALTER TABLE transaction_compte_courant
    ADD CONSTRAINT transaction_compte_courant_type_check
        CHECK (type IN ('EMPRUNT', 'DETTE_INITIALE', 'REMBOURSEMENT'));
