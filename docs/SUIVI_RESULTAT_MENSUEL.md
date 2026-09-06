# Suivi du résultat mensuel Fish-Cam

## Décision métier

Fish-Cam est utilisé comme outil de saisie différée : le patron filme les cahiers,
les données sont saisies chaque semaine, puis l'application produit le récapitulatif
du mois. La priorité n'est donc pas une caisse en temps réel, mais un résultat mensuel
de gestion fiable.

Le résultat d'une période est calculé ainsi :

```text
résultat = encaissements - achats - charges
         + stock final - stock initial
         + créances finales - créances initiales
```

Les encaissements peuvent contenir des remboursements d'anciennes dettes. La variation
des créances corrige cet effet. Cette formule suppose que les achats enregistrés sont
bien ceux de la période et qu'aucune dette fournisseur importante n'est omise.

## Premier relevé : 30 septembre 2026

Le premier comptage physique sera effectué le 30 septembre. Il constitue :

- le stock final connu de septembre ;
- le premier point de référence disponible ;
- le stock initial utilisable pour octobre.

Comme aucun relevé fiable d'ouverture n'existe pour septembre, Fish-Cam doit afficher
septembre comme **PROVISOIRE**. Il ne faut jamais remplacer un montant inconnu par zéro.
Après un nouveau relevé à la fin d'octobre, octobre pourra être marqué **VALIDÉ**.

Le patron peut compter le détail des poissons sur papier, puis transmettre uniquement
la valeur totale par boutique. La valeur est calculée au coût d'achat, jamais au prix
de vente. La photo ou la feuille détaillée est conservée comme justificatif.

Le même relevé contient le total des sommes encore dues par les clients de la boutique.
Le détail client reste géré par les comptes courants lorsque cette fonction est utilisée.

## Reprise des anciennes dettes

L'opération **« Dette initiale / reprise du cahier »** est disponible lors de la régularisation
du compte d'un client. Elle permet de reprendre progressivement le cahier sans transformer
une ancienne dette saisie aujourd'hui en `EMPRUNT` du jour, ce qui fausserait la clôture et
les ventes à crédit de la période courante.

Cette opération :

- ajuste le solde du client et conserve la date d'origine lorsqu'elle est connue ;
- utilise le type distinct `DETTE_INITIALE` ;
- n'entre pas dans `montantDettesJour` ni dans la clôture du jour ;
- reste visible et auditée dans l'historique du compte ;
- permet ensuite les remboursements ordinaires sur ce solde ;
- peut dépasser la limite de crédit pendant la reprise, mais les nouveaux emprunts restent
  ensuite soumis à cette limite.

Une dette initiale mal recopiée n'est jamais modifiée ni supprimée silencieusement. La
correction crée un mouvement `ANNULATION_DETTE_INITIALE`, puis une nouvelle dette avec la
bonne valeur. Mettre le nouveau montant à zéro réalise une annulation complète. Le motif,
l'utilisateur et les deux lignes restent visibles dans l'historique.

La création de la fiche et du compte client précède la reprise de sa dette. Une dette déjà
reprise dans le compte client est incluse dans le total des créances du relevé mensuel ; elle
ne doit pas être ajoutée une seconde fois comme nouvel emprunt.

## Contrôle automatique des créances

Pour chaque relevé de fin de mois, FishCam reconstitue le total encore dû depuis les comptes
clients à la date du relevé. Les dettes initiales et leurs annulations utilisent la date du
cahier lorsqu'elle est renseignée ; les emprunts et remboursements ordinaires utilisent leur
date réelle de saisie.

L'écran et le PDF comparent :

- **FishCam** : somme automatique des soldes débiteurs par client ;
- **Cahier** : montant total déclaré par le patron dans le relevé ;
- **Écart** : cahier moins FishCam.

Le montant du cahier reste la valeur utilisée dans le résultat mensuel. Le total automatique
est un contrôle : tant que tout le cahier n'a pas été repris, il peut être inférieur. Lorsque
la reprise est terminée, l'écart attendu est zéro ; sinon il faut rechercher un client, une
dette, un remboursement ou une correction manquante.

## Charges mensuelles communiquées

| Catégorie | Bare | Ville | Lele |
|---|---:|---:|---:|
| Loyer | 15 000 | 35 000 | 15 000 |
| Électricité | 32 000 | 72 500 | 30 400 |
| Transport | 90 000 | 31 000 | 20 000 |
| Taxes | 15 000 | 25 000 | 15 000 |
| Salaires | 100 000 | 130 000 | 100 000 |
| Ration | 31 000 | 45 000 | 15 000 |
| **Total** | **283 000** | **338 500** | **195 400** |

Charges générales connues : direction générale 340 000 FCFA par mois et manutention
Congelcam 70 000 FCFA par mois, sous réserve que cette dernière ne soit pas déjà comprise
dans les transports des boutiques.

Ces montants sont des paramètres métier saisis dans l'application. Ils ne doivent pas
être codés en dur : une date de début et une date de fin permettent de conserver
l'historique lorsqu'un montant change.

## Comportement attendu du backend

1. Les clôtures journalières existantes fournissent achats et encaissements.
2. Les charges récurrentes applicables au mois sont ajoutées automatiquement.
3. Les charges ponctuelles du mois sont ajoutées une seule fois.
4. Le relevé daté du dernier jour du mois précédent est la situation d'ouverture.
5. Le relevé daté du dernier jour du mois calculé est la situation de clôture.
6. Le stock et les créances sont indépendants. Dès que les deux relevés de stock existent,
   `resultatCorrigeStock` est calculé, même si les créances restent inconnues : le comptage
   physique corrige la plus grosse erreur et ne doit pas attendre le cahier des dettes.
7. `resultatValide` n'est renseigné qu'avec le stock **et** les créances aux deux bornes.
   Une créance inconnue reste `NULL`, elle ne devient jamais zéro.
8. Les charges générales sont retirées une seule fois du résultat global.
9. Le résultat global n'est jamais plus sûr que sa boutique la moins bien renseignée.

### Statuts

| Statut | Signification |
|---|---|
| `PROVISOIRE` | Au moins un relevé de stock manque. Seul `resultatProvisoire` existe. |
| `CORRIGE_STOCK` | Les deux stocks sont connus, les créances non. `resultatCorrigeStock` est fiable. |
| `ESTIME` | Stock et créances connus, mais au moins un relevé est une estimation. |
| `VALIDE` | Stock et créances connus, comptage physique aux deux bornes. |

### Alertes de double comptage

Le transport et la ration existent à deux endroits : en dépense de clôture journalière et
en charge mensuelle. Renseigner les deux déduirait la même dépense deux fois. Lorsque les
deux sources sont non nulles sur le même mois, la réponse contient une `alerte` nommant les
deux montants. Il faut alors corriger la saisie pour ne conserver qu'une seule source.

Une charge de gestion ne peut pas non plus être saisie deux fois : un même libellé, dans la
même catégorie et sur le même périmètre, est refusé dès que les périodes se recouvrent.
Pour remplacer une charge existante, il faut d'abord la terminer avec `PATCH .../terminer`.

Le rapport doit distinguer clairement `resultatProvisoire`, `resultatCorrigeStock` et
`resultatValide`. Le terme « bénéfice net » ne doit pas être utilisé lorsque le stock ou les
créances sont inconnus.

Les dépenses déjà saisies dans les clôtures journalières sont déduites séparément. Une
charge mensuelle ne doit donc pas reprendre une dépense déjà enregistrée au jour le jour,
sinon elle serait comptée deux fois.

## Travail mensuel

1. Saisir les achats et clôtures reçus dans les cahiers jusqu'au dernier jour du mois.
2. Vérifier ou actualiser les charges récurrentes ; ajouter les charges ponctuelles.
3. Le dernier jour, compter le stock de chaque boutique et le valoriser au coût d'achat.
4. Noter, pour chaque boutique, le total des créances clients encore ouvertes.
5. Enregistrer un relevé unique par boutique avec ces deux montants.
6. Générer le résultat par boutique et le résultat global.

Pour septembre 2026, l'étape 4 peut commencer par un total reconstitué depuis le cahier
des dettes. Comme la situation du 31 août est inconnue, septembre restera provisoire.
Le relevé du 30 septembre servira automatiquement d'ouverture au calcul d'octobre.

Si les créances ne sont pas reconstituées, le comptage de stock n'est pas perdu pour autant :
octobre sortira en `CORRIGE_STOCK`, avec un résultat déjà corrigé de la marchandise invendue.

Les charges stables (loyer, salaire fixe) sont créées comme récurrentes. Une charge qui
varie réellement selon le mois (facture d'électricité, réparation exceptionnelle) est
saisie avec le montant du mois comme charge ponctuelle, afin de ne pas utiliser une
moyenne à la place du montant payé ou dû.

## API ajoutée

- `POST /api/v1/charges-gestion` : créer une charge récurrente ou ponctuelle ;
- `GET /api/v1/charges-gestion?mois=10&annee=2026` : charges générales du mois ;
- ajouter `poissonnerieId` à cette requête pour les charges d'une boutique ;
- `PATCH /api/v1/charges-gestion/{id}/terminer?dateFin=2026-12-31` : arrêter une
  charge récurrente sans fausser les anciens résultats ;
- `POST /api/v1/releves-mensuels` : enregistrer stock et créances à une date ;
- `GET /api/v1/releves-mensuels?poissonnerieId=1` : historique des relevés ;
- `GET /api/v1/resultats-mensuels/boutique?poissonnerieId=1&mois=10&annee=2026` ;
- `GET /api/v1/resultats-mensuels/global?mois=10&annee=2026` ;
- `GET /api/v1/exports/resultat-mensuel/pdf?mois=10&annee=2026` : rapport patron
  consolidé des trois boutiques.

Les anciens récapitulatifs par boutique restent les documents détaillés des achats et
encaissements saisis. Leur ancien « bénéfice de la période » devient « solde des saisies »,
car seul le rapport de résultat mensuel inclut les charges récurrentes, les charges
générales, le stock et les créances. L'archive mensuelle conserve ces PDF détaillés et
ajoute le rapport consolidé destiné au patron.

Exemple de relevé du 30 septembre :

```json
{
  "poissonnerieId": 1,
  "dateReleve": "2026-09-30",
  "valeurStock": 200000,
  "totalCreancesClients": 125000,
  "modeEvaluation": "COMPTAGE_PHYSIQUE",
  "note": "Valeur totale issue de la fiche papier conservée"
}
```
