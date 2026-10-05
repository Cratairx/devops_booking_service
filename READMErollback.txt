Rollback

Varje image taggas med workflowens run number (t.ex. :26, :27). Taggarna skrivs aldrig över, så alla gamla versioner ligger kvar på Docker Hub. staging och production är bara pekare till någon av dem.

Rollback av produktion
Kolla vilken version som funkade senast. Det ser du under Actions (tidigare körningar av "CI and build docker image") eller bland taggarna på Docker Hub.
Gå till Actions → CI and build docker image → Run workflow.
Skriv in run number du vill tillbaka till, t.ex. 25, och kör.
deploy_production flyttar production-taggen till den äldre imagen. Railway plockar upp den själv via Image Auto Updates. Vill du ha det direkt kan du klicka Redeploy på servicen i Railway.

Det görs ingen ny build, så du får exakt samma image som tidigare. Det gör rollbacken snabb och säker.

Rollback av staging

Staging följer alltid main. Gör en git revert av den felaktiga ändringen i en ny PR. När den mergas byggs en ny image och staging uppdateras av sig själv.
