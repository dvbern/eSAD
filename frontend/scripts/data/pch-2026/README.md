# Wachstumskurven 2026 — source reference data

These CSVs are the reference values behind the `*-2026.ts` Perzentil curves in
`libs/esc-app/feat/feat-anlass/.../untersuchung-groessegewicht/config/`.

They were transcribed verbatim from the supplementary material of:

> Eiholzer U, Stephan A, Dubinski I, Fritz C, Noordam C (2025).
> *Updated Swiss Growth References 2025: No Height Differences, but BMI Variations Associated with
> Migration.* J. Clin. Med. 14, 5912. <https://doi.org/10.3390/jcm14165912>

Supplementary file `jcm-14-05912-s001.zip`, *"Supplemental material to Manuscript Cohort 2019+2025,
Supp. Table S1–S8, Figure S1–S3"*. The article is open access (CC BY); the free full text is also on
[PMC12387566](https://pmc.ncbi.nlm.nih.gov/articles/PMC12387566/).

These are the curves recommended by **pädiatrie schweiz** as the *Wachstumskurven 2026*
(`PCH_Wachstumskurven_260616_DE.pdf`, dated 10.06.2026).

## Files

| File | Source table | x column | Range |
| --- | --- | --- | --- |
| `height-girls.csv` | S1 | `age` (years) | 0–18 |
| `height-boys.csv` | S2 | `age` (years) | 0–20 |
| `bmi-girls.csv` | S3 | `age` (years) | 2–18 |
| `bmi-boys.csv` | S4 | `age` (years) | 2–20 |
| `weight-for-height-girls.csv` | S7 (50–180) + delivery (181–190) | `height` (cm) | 50–190 |
| `weight-for-height-boys.csv` | S8 | `height` (cm) | 50–200 |

The full published range is kept here even where eSAD plots only part of it — the narrowing to the
ranges actually charted happens in the generator, not in the data.

The girls' Gewicht-für-Grösse table (S7) stops at 180 cm. Rows 181–190 cm of
`weight-for-height-girls.csv` are taken verbatim from `centiles female WeightForHeight.csv` (`l`
and `s` from `LMS female WeightForHeight.csv`), delivered by pädiatrie schweiz (ESC-1306). They
are in 1-cm steps and 2 decimals, as delivered.

## Columns

`p3 … p97` are the **smoothed percentile values** as published, so the curves are taken directly from
the paper rather than recomputed. `l` and `s` are the LMS model parameters (the median `m` is the `p50`
column); they are not needed for the seven percentile lines and are kept only so SD lines can be
derived later without going back to the paper.

The BMI tables carry two extra columns, `ow` and `ob` — the overweight and obesity cut-off values in
kg/m². Unused today; they belong to the deferred Adipositas zones.

## Regenerating the curves

```bash
npx tsx scripts/generate-percentile-distributions.ts
```

Do not hand-edit the generated `*-2026.ts` files — change the CSVs or the generator instead.
