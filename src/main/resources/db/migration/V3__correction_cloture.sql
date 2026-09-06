ALTER TABLE public.cloture_journaliere
    ADD COLUMN derniere_correction_motif character varying(500),
    ADD COLUMN modifie_le timestamp(6) without time zone,
    ADD COLUMN modifie_par_id bigint;

ALTER TABLE public.cloture_journaliere
    ADD CONSTRAINT fk_cloture_modifie_par
        FOREIGN KEY (modifie_par_id) REFERENCES public.app_user(id);

CREATE INDEX idx_cloture_modifie_par
    ON public.cloture_journaliere (modifie_par_id);
