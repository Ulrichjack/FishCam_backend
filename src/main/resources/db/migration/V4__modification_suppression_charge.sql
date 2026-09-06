ALTER TABLE public.charge_gestion
    ADD COLUMN supprimee boolean NOT NULL DEFAULT false,
    ADD COLUMN supprimee_le timestamp(6) without time zone,
    ADD COLUMN supprimee_par_id bigint;

ALTER TABLE public.charge_gestion
    ADD CONSTRAINT fk_charge_gestion_supprimee_par
        FOREIGN KEY (supprimee_par_id) REFERENCES public.app_user(id);

CREATE INDEX idx_charge_gestion_supprimee
    ON public.charge_gestion (supprimee);
