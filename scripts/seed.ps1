param(
    [string]$Container = "wallet-ledger-postgres-1"
)
Get-Content "$PSScriptRoot\seed.sql" | docker exec -i $Container psql -U wallet -d wallet
