# Vérifie dans quel shell tu es
echo $SHELL

# Ajoute les alias dans le BON fichier (ça marche pour bash ET zsh)
cat >> ~/.bashrc << 'EOF'

# ============ FISHCAM ALIAS — LA VIE EN 1 LETTRE ============
alias f="psql -h localhost -U fishcam_user -d fishcam_db"
alias ff="psql -h localhost -U fishcam_user -d fishcam_db"  # sans mdp si ~/.pgpass existe
alias t="f -c '\dt'"
alias det="f -c 'SELECT id, client_id, remaining_amount, statut FROM dette ORDER BY updated_at DESC LIMIT 10;'"
alias cli="f -c 'SELECT id, first_name, last_name, phone FROM client ORDER BY created_at DESC LIMIT 10;'"
alias fbak="pg_dump fishcam_db > ~/fishcam_backup_$(date +%Y-%m-%d_%H%M).sql && echo 'Backup → ~/fishcam_backup_$(date +%Y-%m-%d_%H%M).sql'"
EOF

# Si tu es en zsh, on fait aussi la même chose
[ -f ~/.zshrc ] && cat ~/.bashrc >> ~/.zshrc

# Recharge tout
source ~/.bashrc 2>/dev/null || source ~/.zshrc
echo "Alias FISHCAM installés pour TOUJOURS ! Tape 'f' pour tester"

