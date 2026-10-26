# Jasse Keyboard – första testversionen

Utvecklingsgren: `jasse/first-test-apk` i [din fork](https://github.com/jjassee98/DictateKeyboard).
Utgångspunkt: Dictate Keyboard `39f33c968995796d6857f56b07df148df6ba963b` (24 oktober 2026).
Originalets Apache-2.0-licens, upphovsrättsangivelser och beroenderedovisning behålls.

## Installera på Galaxy S23 Ultra eller Tab S10 Ultra

1. Ladda ned `Jasse-Keyboard-test.apk` på mobilen eller surfplattan och öppna filen.
2. Om Samsung/Android blockerar installationen: ge tillfälligt webbläsaren eller Mina filer
   tillåtelse att installera okända appar. Kontrollera även eventuell Samsung Auto Blocker-inställning.
3. Öppna **Jasse Keyboard Test**. Följ appens guide för att aktivera och välja tangentbordet.
   Samsung visar normalt alternativen under **Inställningar → Allmän hantering →
   Tangentbordslista och standard**. Menynamn kan variera med One UI-version.
4. Lägg till **Svenska** och **English** under tangentbordets språk. Aktivera flerspråkig
   inmatning under **Skrivning/Typing** om båda språken ska skyddas samtidigt.
5. Kontrollera att ordförslag, autokorrigering, förbättrad svensk stavning och saknade
   mellanslag är aktiva. Börja med balanserad rättningsstyrka.
6. Välj **Jasse AMOLED**, **Jasse Rosa**, **Jasse Lila** eller **Jasse Neon** under **Tema**.
   Originalets ljusa/mörka teman och redigerare finns kvar. För surfplattan: prova delat
   tangentbord och justera höjden med originalets layoutinställningar.
7. Konfigurera önskad AI-/dikteringsleverantör i appen. Inga API-nycklar följer med APK:n.

Testappen använder paketnamnet **`se.jasse.keyboard.debug`**. Det är ett annat paket än
Dictate (`net.devemperor.dictate`), så båda kan installeras parallellt med separata appdata.
Release-paketets basnamn är `se.jasse.keyboard`. Kotlin-namnrymden behålls för kompatibilitet
med originalkoden. Detta är en felsökningssignerad testversion, inte en produktionsrelease.

## Bygg utan dator

1. Öppna [Actions i din fork](https://github.com/jjassee98/DictateKeyboard/actions) i webbläsaren.
2. Forkar kan kräva att du först väljer **I understand my workflows, go ahead and enable them**.
3. Välj **Jasse test APK** och **Run workflow**. Välj grenen `jasse/first-test-apk`.
   Om knappen inte visas finns arbetsflödet ännu bara på utvecklingsgrenen: varje push till
   `jasse/**` startar det ändå. När ändringen senare slagits samman till `main` blir manuell
   start från Actions normalt tillgänglig.
4. När körningen är grön: öppna den, gå till **Artifacts**, ladda ned
   **Jasse-Keyboard-test-N** och packa upp ZIP-filen i **Mina filer**.
5. Öppna APK:n. Artefakten innehåller även installationsguiden och SHA-256-kontrollsumman.
   APK-artefakter sparas i 30 dagar; källkoden finns kvar och kan byggas igen.

Flödet installerar Android SDK 36, NDK 29 och CMake, hämtar checksummeverifierade
talbibliotek, bygger den lokala översättningsmotorn, kör tester och bygger en signerad debug-APK.
ARM64 väljs för dina två enheter så att onödiga emulator-/32-bitarsbibliotek inte behöver laddas ned.
Native-biblioteken cachas efter sina byggindata. Tal-/AI-tjänster kontaktas inte av regressionstesterna.

### Signering och uppdateringar

GitHub genererar normalt en ny debug-nyckel på varje ny runner. Android accepterar bara en
uppdatering om paketnamn och signeringscertifikat matchar. APK:er med olika certifikat kräver
avinstallation först, vilket raderar appdata. Spara inställningar vid behov med appens backup.

Arbetsflödet stöder en **valfri GitHub Actions-secret `JASSE_TEST_KEYSTORE_BASE64`** för samma
testcertifikat mellan byggen. Innehållet ska vara Base64 av en separat Android debug-keystore
med alias `androiddebugkey` och standardlösenord `android`. Nyckeln ska lagras som en
Actions-secret, aldrig i repositoryt eller en artefakt. Den här nyckeln ska inte användas för
produktion. Inställningen finns under repositoryts **Settings → Secrets and variables →
Actions → New repository secret**. Ingen sådan secret har skapats automatiskt i denna etapp.

## Vad som är nytt

| Område | Första Jasse-versionen |
| --- | --- |
| Svenska offline | 73 756 ord samt 150 000 ordpar och 100 000 ordtripplar i APK:n |
| Vanliga svenska fel | Prioriterade stavningsförslag i befintlig språkprovider; separata reglage |
| Saknade mellanslag | Förslag på t.ex. `du märker`; välbelagda pronomenfraser kan rättas vid mellanslag |
| Skydd | Nya regler respekterar kända/egna ord, flerspråkighet och tangenttryckshistorik |
| Teman | Fyra nya förinställningar i originalets befintliga temaredigerare |
| Egen identitet | Jasse-namn, separat paketnamn och separat testcertifikat |
| Molnbygge | GitHub Actions med test-APK, kontrollsumma och testrapporter |

Se [SWEDISH-DATA.md](SWEDISH-DATA.md) för datakällor, licenser och reglernas begränsningar.

## Befintligt och nästa etapper

Originalet har redan AI-leverantörer, avancerad diktering, egna promptåtgärder, flerspråkig
ordskyddning, lokal ordinlärning, prediktion, svepskrivning, temaredigering/import/export,
egen bakgrund, sifferrad, delat tangentbord och justerbar storlek. Dessa delar har bevarats.
Det betyder inte att samtliga funktioner är verifierade på Samsung Android 16.

| Önskemål | Befintligt och kvar att utveckla |
| --- | --- |
| Temaredigerare | Befintlig regelbaserad redigerare, sparade teman, förhandsgranskning och ljus/mörk-växling. Enklare samlad redigerare med fler direkta reglage återstår. |
| Ljud | Befintliga Android-knappljud, volym, separat ljud för specialtangenter och inställning att åsidosätta systemets tryckljud. Egna SoundPool-paket, filimport och sparade ljudprofiler återstår. |
| Urklipp | Befintlig sökning, text/bilder, fästa objekt, historikgränser, rensning och känslighetsmarkering. Granska fler Samsung-beteenden på enhet. |
| Förslagsfält | Befintligt tema, smartbar och åtgärdsredigering. Separata enkla stilprofiler för ord-/AI-rad, fritt antal förslag och fler placeringar återstår. |
| AI-verktyg | Befintliga leverantörer, promptbibliotek och egna åtgärder. Samlad profilredigerare, förhandsvisning av varje ersättning och verifierad ångrahantering återstår. |
| Svensk kvalitet | Praktisk provning och en större oberoende felkorpus behövs; ingen utfästelse om Gboard-nivå. Utvärdera öppna lexikon och morfologi som nästa steg. |
| Surfplatta | Befintlig responsiv layout och delat tangentbord. Nya särskilda Samsung-layoutprofiler och bättre svepmodell återstår. |

### Urklipp och skärmdumpar på Android 16

Android ger normalt urklippsåtkomst till appen med fokus eller den valda standard-IME:n.
Det betyder inte att alla skärmdumpar hamnar i urklippet. En skärmdump som Samsung bara
sparar som fil kan inte fångas av en vanlig urklippslyssnare. När Android/Samsung faktiskt
kopierar en bild och ger en läsbar URI kan den befintliga bildhanteringen visa den. Automatisk
övervakning av hela bildgalleriet, MediaProjection eller en ny tillgänglighetstjänst har inte lagts till.
Känslighetsflaggan kommer från den kopierande appen; allt känsligt innehåll är inte automatiskt märkt.

### Integritet och kompatibilitet

De nya språkreglerna arbetar lokalt. De skickar ingen skriven text till en tjänst och lagrar ingen
ytterligare personlig text. Originalets inkognito-/lösenordshantering, backupredigering av
hemligheter och leverantörsval bevaras. API-nycklar hanteras fortfarande av originalets
appprivata preferenser; denna etapp inför inget nytt separat Keystore-valv och är ingen full
säkerhetsrevision. Android 12+-backup behåller originalets krav på krypterad molnbackup.

Play-köp/Dictate Cloud och Wear OS-kompanjonens signaturkoppling kan vara bundna till
originalets paket/signering. De ska inte betraktas som fungerande i en ompaketerad fork utan
separat verifiering. Egna leverantörsnycklar och lokala modeller finns kvar i koden.

## Enhetsprovning

Ingen fysisk Samsung-enhet är ansluten till molnmiljön. Installationsprov, mikrofon, ljud,
latens, AI-anrop, bildurklipp och layout ska därför provas på båda enheterna:

- Skriv `igentligen`, `faktist`, `tillsamans` och `jagvet`, sedan mellanslag. Ångra med Backspace.
- Skriv `dumärker` och välj `du märker` i förslagsraden.
- Skriv avsiktliga namn och lägg vid behov in dem i ordlistan. Prova svenska och engelska ihop.
- Stäng av autokorrigering: förslag ska finnas kvar utan automatisk ersättning.
- Diktera korrekt svensk text, fortsätt skriva och kontrollera att texten inte ändras oväntat.
- Prova lösenordsfält och inkognito; byt tema, stående/liggande läge och surfplattans delade layout.
- Kopiera text och en bild; prova sökning, fästa objekt, rensning och känsligt innehåll.

Lokalt bygge: `./gradlew :app:assembleDebug -PjasseArm64Only=true` efter att SDK och native-
biblioteken förberetts enligt arbetsflödet. Utelämna flaggan för APK med alla originalets ABI:er.
