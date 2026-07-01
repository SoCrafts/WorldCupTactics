#!/usr/bin/env python3
"""
merge_players.py
────────────────
Merges players_from_kaggle.csv + teams_from_kaggle.csv into a single
players.csv that the Android app can consume via CsvPlayerDataSource.

Input files (both in the same directory as this script's target):
  app/src/main/res/raw/players_from_kaggle.csv
  app/src/main/res/raw/teams_from_kaggle.csv

Output file:
  app/src/main/res/raw/players.csv

Output columns (9):
  player_id, player_name, team, jersey_number, position,
  nationality, height_cm, weight_kg, club_name

Jersey number assignment strategy (no jersey data in source):
  Within each squad of 26, assign numbers 1-26 in position order:
    GK  → 1, 12, 23          (up to 3 GKs)
    DEF → 2, 3, 4, 5, 6 …   (fills slots after GK block)
    MID → next available
    FWD → next available
  Any remaining players continue sequentially.

Team name normalisation:
  The app's TeamIdMapper uses specific English names that must match
  exactly. This script normalises kaggle names → canonical app names.

Usage:
  python3 scripts/merge_players.py
"""

import csv
import os
import re
import unicodedata

# ─────────────────────────────────────────────────────────────────────────────
# Paths
# ─────────────────────────────────────────────────────────────────────────────

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(SCRIPT_DIR)
RAW_DIR = os.path.join(PROJECT_ROOT, "app", "src", "main", "res", "raw")

PLAYERS_SRC = os.path.join(RAW_DIR, "players_from_kaggle.csv")
TEAMS_SRC = os.path.join(RAW_DIR, "teams_from_kaggle.csv")
OUTPUT = os.path.join(RAW_DIR, "players.csv")

# ─────────────────────────────────────────────────────────────────────────────
# Kaggle team name  →  canonical app name used in TeamIdMapper
# ─────────────────────────────────────────────────────────────────────────────

TEAM_NAME_MAP = {
    "Mexico":                 "Mexico",
    "South Africa":           "South Africa",
    "South Korea":            "South Korea",
    "Czechia":                "Czechia",
    "Canada":                 "Canada",
    "Bosnia and Herzegovina": "Bosnia-Herzegovina",
    "Qatar":                  "Qatar",
    "Switzerland":            "Switzerland",
    "Brazil":                 "Brazil",
    "Morocco":                "Morocco",
    "Haiti":                  "Haiti",
    "Scotland":               "Scotland",
    "USA":                    "United States",
    "Paraguay":               "Paraguay",
    "Australia":              "Australia",
    "Türkiye":                "Turkey",
    "Germany":                "Germany",
    "Curaçao":                "Curacao",
    "Côte d'Ivoire":          "Ivory Coast",
    "Ecuador":                "Ecuador",
    "Netherlands":            "Netherlands",
    "Japan":                  "Japan",
    "Sweden":                 "Sweden",
    "Tunisia":                "Tunisia",
    "Belgium":                "Belgium",
    "Egypt":                  "Egypt",
    "IR Iran":                "Iran",
    "New Zealand":            "New Zealand",
    "Spain":                  "Spain",
    "Cabo Verde":             "Cape Verde",
    "Saudi Arabia":           "Saudi Arabia",
    "Uruguay":                "Uruguay",
    "France":                 "France",
    "Senegal":                "Senegal",
    "Iraq":                   "Iraq",
    "Norway":                 "Norway",
    "Argentina":              "Argentina",
    "Algeria":                "Algeria",
    "Austria":                "Austria",
    "Jordan":                 "Jordan",
    "Portugal":               "Portugal",
    "Congo DR":               "DR Congo",
    "Uzbekistan":             "Uzbekistan",
    "Colombia":               "Colombia",
    "England":                "England",
    "Croatia":                "Croatia",
    "Ghana":                  "Ghana",
    "Panama":                 "Panama",
}

# Kaggle nationality column not present; derive from team name
# (national team players represent their country)
TEAM_TO_NATIONALITY = {v: v for v in TEAM_NAME_MAP.values()}

# ─────────────────────────────────────────────────────────────────────────────
# Position normalisation: kaggle (GK/DEF/MID/FWD) → app canonical
# ─────────────────────────────────────────────────────────────────────────────

POS_MAP = {
    "GK":  "Goalkeeper",
    "DEF": "Defender",
    "MID": "Midfielder",
    "FWD": "Forward",
}

# Position group order for jersey assignment within a squad
POS_ORDER = ["GK", "DEF", "MID", "FWD"]

# ─────────────────────────────────────────────────────────────────────────────
# Name cleaning
# ─────────────────────────────────────────────────────────────────────────────

def clean_name(raw: str) -> str:
    """
    Strips extra whitespace and repeated words from the kaggle player names.
    Some names have the last name duplicated (e.g. 'Lamine Yamal Yamal').
    Also normalises Unicode to NFC so accented characters are preserved cleanly.
    """
    name = unicodedata.normalize("NFC", raw.strip())
    # Collapse multiple spaces
    name = re.sub(r" {2,}", " ", name)
    # Remove a trailing duplicated word (e.g. "Yamal Yamal" → "Yamal",
    # "GROß GROß Gross" → keep as-is unless it's a simple exact duplicate)
    words = name.split()
    if len(words) >= 2 and words[-1] == words[-2]:
        words = words[:-1]
    # Handle "GROß GROß Gross" style: if 3rd-to-last == 2nd-to-last (case-insensitive ascii)
    if len(words) >= 3:
        w2 = words[-2].lower().replace("ß", "ss")
        w3 = words[-3].lower().replace("ß", "ss")
        if w2 == w3:
            words = words[:-2] + [words[-1]]
    return " ".join(words)


# ─────────────────────────────────────────────────────────────────────────────
# Jersey number assignment
# ─────────────────────────────────────────────────────────────────────────────

def assign_jersey_numbers(squad: list[dict]) -> list[dict]:
    """
    Assigns jersey numbers 1-26 to a 26-player squad in position order.
    Standard football numbering convention:
      GK  → 1 (starter), then 12, 23
      DEF → 2, 3, 4, 5 (starters), then continue
      MID → next slots
      FWD → last slots
    """
    by_pos = {p: [] for p in POS_ORDER}
    for player in squad:
        pos = player["position"]
        by_pos.get(pos, by_pos["FWD"]).append(player)

    ordered = []
    for pos in POS_ORDER:
        ordered.extend(by_pos[pos])

    for i, player in enumerate(ordered):
        player["jersey_number"] = i + 1

    return ordered


# ─────────────────────────────────────────────────────────────────────────────
# Main
# ─────────────────────────────────────────────────────────────────────────────

def main():
    # 1. Load teams
    with open(TEAMS_SRC, newline="", encoding="utf-8") as f:
        teams_raw = list(csv.DictReader(f))

    teams_by_id = {}
    for t in teams_raw:
        kaggle_name = t["team_name"]
        canonical = TEAM_NAME_MAP.get(kaggle_name, kaggle_name)
        teams_by_id[t["team_id"]] = {
            "kaggle_name": kaggle_name,
            "canonical_name": canonical,
        }

    # 2. Load players and group by team
    with open(PLAYERS_SRC, newline="", encoding="utf-8") as f:
        players_raw = list(csv.DictReader(f))

    squads: dict[str, list] = {}
    for p in players_raw:
        tid = p["team_id"]
        squads.setdefault(tid, []).append(p)

    # 3. Assign jersey numbers per squad and build output rows
    output_rows = []
    for tid, squad in sorted(squads.items(), key=lambda x: int(x[0])):
        team_info = teams_by_id.get(tid, {"canonical_name": tid, "kaggle_name": tid})
        canonical_team = team_info["canonical_name"]

        # Sort by position group before numbering
        squad_sorted = sorted(squad, key=lambda p: POS_ORDER.index(p["position"]))
        numbered = assign_jersey_numbers(squad_sorted)

        for player in numbered:
            raw_name = player["player_name"]
            clean = clean_name(raw_name)
            pos_canonical = POS_MAP.get(player["position"], player["position"])

            output_rows.append({
                "player_id":     player["player_id"],
                "player_name":   clean,
                "team":          canonical_team,
                "jersey_number": player["jersey_number"],
                "position":      pos_canonical,
                "nationality":   canonical_team,   # national team players = team nationality
                "height_cm":     player["height_cm"],
                "weight_kg":     "",               # not in source, left blank
                "club_name":     player["club_team"],
            })

    # 4. Write output
    fieldnames = [
        "player_id", "player_name", "team", "jersey_number", "position",
        "nationality", "height_cm", "weight_kg", "club_name",
    ]
    tmp = OUTPUT + ".tmp"
    with open(tmp, "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(output_rows)

    os.replace(tmp, OUTPUT)

    print(f"✓ Written {len(output_rows)} players → {OUTPUT}")
    print(f"  Teams: {len(squads)}")
    print(f"  File size: {os.path.getsize(OUTPUT):,} bytes")

    # 5. Quick sanity check — print Spain's squad
    spain = [r for r in output_rows if r["team"] == "Spain"]
    if spain:
        print("\n  Spain squad sample:")
        for p in spain[:5]:
            print(f"    #{p['jersey_number']:2d}  {p['player_name']:30s}  {p['position']}")


if __name__ == "__main__":
    main()
