-- Charges mensuelles communiquees par le patron pour les trois boutiques.
-- Chaque insertion est idempotente au niveau metier : une categorie deja configuree
-- sur le meme perimetre pour aout 2026 n'est jamais ajoutee une seconde fois.

WITH createur AS (
    SELECT id
    FROM public.app_user
    WHERE role IN ('PATRON', 'SUPER_ADMIN')
    ORDER BY CASE WHEN role = 'PATRON' THEN 0 ELSE 1 END, id
    LIMIT 1
), charges_boutiques(motif_nom, categorie, libelle, montant) AS (
    VALUES
        ('BARE',  'LOYER',       'Loyer BARE',          15000.00),
        ('BARE',  'ELECTRICITE', 'Électricité BARE',    32000.00),
        ('BARE',  'TRANSPORT',   'Transport BARE',      90000.00),
        ('BARE',  'TAXES',       'Taxes BARE',          15000.00),
        ('BARE',  'SALAIRE',     'Salaires BARE',      100000.00),
        ('BARE',  'RATION',      'Ration BARE',         31000.00),
        ('VILLE', 'LOYER',       'Loyer VILLE',         35000.00),
        ('VILLE', 'ELECTRICITE', 'Électricité VILLE',   72500.00),
        ('VILLE', 'TRANSPORT',   'Transport VILLE',     31000.00),
        ('VILLE', 'TAXES',       'Taxes VILLE',         25000.00),
        ('VILLE', 'SALAIRE',     'Salaires VILLE',     130000.00),
        ('VILLE', 'RATION',      'Ration VILLE',        45000.00),
        ('LELE',  'LOYER',       'Loyer LELE',          15000.00),
        ('LELE',  'ELECTRICITE', 'Électricité LELE',    30400.00),
        ('LELE',  'TRANSPORT',   'Transport LELE',      20000.00),
        ('LELE',  'TAXES',       'Taxes LELE',          15000.00),
        ('LELE',  'SALAIRE',     'Salaires LELE',      100000.00),
        ('LELE',  'RATION',      'Ration LELE',         15000.00)
)
INSERT INTO public.charge_gestion (
    poissonnerie_id, categorie, libelle, montant, recurrente, date_debut,
    date_fin, active, supprimee, created_by_id, created_at, updated_at
)
SELECT p.id, cb.categorie, cb.libelle, cb.montant, true, DATE '2026-08-01',
       NULL, true, false, createur.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM charges_boutiques cb
JOIN public.poissonnerie p ON UPPER(p.name) LIKE '%' || cb.motif_nom || '%'
CROSS JOIN createur
WHERE p.active = true
  AND NOT EXISTS (
      SELECT 1 FROM public.charge_gestion c
      WHERE c.poissonnerie_id = p.id
        AND c.categorie = cb.categorie
        AND c.supprimee = false
        AND c.date_debut <= DATE '2026-08-31'
        AND (c.date_fin IS NULL OR c.date_fin >= DATE '2026-08-01')
  );

WITH createur AS (
    SELECT id
    FROM public.app_user
    WHERE role IN ('PATRON', 'SUPER_ADMIN')
    ORDER BY CASE WHEN role = 'PATRON' THEN 0 ELSE 1 END, id
    LIMIT 1
), charges_generales(categorie, libelle, montant) AS (
    VALUES
        ('SALAIRE',     'Salaire directeur général',     250000.00),
        ('TRANSPORT',   'Transport directeur général',    45000.00),
        ('RATION',      'Ration directeur général',       45000.00),
        ('MANUTENTION', 'Service livreurs Congelcam',     70000.00)
)
INSERT INTO public.charge_gestion (
    poissonnerie_id, categorie, libelle, montant, recurrente, date_debut,
    date_fin, active, supprimee, created_by_id, created_at, updated_at
)
SELECT NULL, cg.categorie, cg.libelle, cg.montant, true, DATE '2026-08-01',
       NULL, true, false, createur.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM charges_generales cg
CROSS JOIN createur
WHERE NOT EXISTS (
    SELECT 1 FROM public.charge_gestion c
    WHERE c.poissonnerie_id IS NULL
      AND c.categorie = cg.categorie
      AND c.supprimee = false
      AND c.date_debut <= DATE '2026-08-31'
      AND (c.date_fin IS NULL OR c.date_fin >= DATE '2026-08-01')
);
