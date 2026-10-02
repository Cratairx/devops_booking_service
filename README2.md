# Branschstrategi
Vi använder Github flow: Det är en simplare variant av trunk-based.
main är vår lång livade branch och denna ska hålla ren och körbar dvs i ett deploybart skick

# Vårat arbets sätt Github Flow
1. Skapa en ny branch från main för varje ticket.
2. Relativt små ändringar i committsen.
3. Skapa en pull request(PR) mot main
4. CI körs då sina tester och bygger docker imagen
5. PR granskas och mergas när CI är grön.
6. Den skapade branchen man har gjort ändringar i raderas
7. Vid merge till main startar CD pipeline, docker image byggs upp och pushas till Docker Hub
8. Imagen deployas automatiskt till staging.
   I staging kan man då se om allt går rätt till innan man skickar till produktion
9. När ändringen är testad i staging startar någon gruppmedlem produktions deployment manuellt i Github Actions
10. På Railway använder vi oss av Auto updates som gör att Railway upptäcker nya imagen automatiskt
    i docker hub.
11. Samma image som testas i staging deployas till produktion
    railway upptäcker den nya imagen och deployar automatiskt till produktion
12. Ändringen är nu live på: https://devopsbookingservice-production.up.railway.app/


# Varför vi har valt Github Flow
Vi är ett litet team och vi använder bara en tjänst booking-service.
Vi valde detta flöde för att det kändes som att Git Flow skulle bli för mycket jobb för oss,
med en mass olika brancher, det skulle leda till väldigt mycket merjobb.
Vi har inga parallella version att underhålla, den senaste versionen i main är den som gäller
Detta flöde ger snabb feedback och det blir färre mergekonflikter, eftersom brancherna är kortlivade och mergas ofta,
gör att alla alltid jobbar mot samma kodbas. Om det skulle bli konflikter så är dem väldigt små och snabblösta.
Vi valde också detta arbetssätt för att det passar CI/CD där pipelinen triggas av push och PR varje merge bygger en ny image
vilket är grunden för CI/CD
Vi använder oss av regler för att säkra kvaliten istället för extra branches. Branch protection och PR krav med grön CI
gör att vi inte behöver en development_branch.


# När skulle vi byta strategi?
Vi skulle byta strategi om projektet var större, eller om vi hade behövt underhålla flera versioner samtidigt,
då skulle Git Flow passa bättre men med tank på uppgiftens storlek kändes det som att Github Flow var det smartaste valen