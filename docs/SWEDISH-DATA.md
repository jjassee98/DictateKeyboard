# Svenska språkdata

Jasse återanvänder Dictates öppna, frekvensbaserade svenska språkdata. Filerna är oförändrade.
De läggs med i APK:n så att rättning, svepordlista och nästa-ord-förslag fungerar utan en första nedladdning.

| Fil i `app/src/main/assets/ime/dict/` | Källa | SHA-256 |
| --- | --- | --- |
| `sv.json` | [glide-dicts-v1/sv.json](https://github.com/DevEmperor/DictateKeyboard/releases/download/glide-dicts-v1/sv.json) | `95dd05108d3035cd7dd501accd4dfa60e20da4ae11adf8f082941fab277f7cee` |
| `sv_bigrams.txt` | [bigram-dicts-v1/sv_bigrams_150k.txt](https://github.com/DevEmperor/DictateKeyboard/releases/download/bigram-dicts-v1/sv_bigrams_150k.txt) | `f413d0743258d3685d518a8b997e7388c1f1c68869ba7c99d41ad22011dc7d1b` |
| `sv_trigrams.txt` | [trigram-dicts-v1/sv_trigrams_100k.txt](https://github.com/DevEmperor/DictateKeyboard/releases/download/trigram-dicts-v1/sv_trigrams_100k.txt) | `dbaeadc182716d9c6650263f9dcc14f541294717145cfb5ce104ba0e284a0dda` |

Ordlistan innehåller 73 756 ord. Kontexttabellerna innehåller 150 000 ordpar och 100 000 ordtripplar.
Ordlistans frekvenser kommer från OPUS/OpenSubtitles; kontexttabellerna från Leipzig Corpora
Collection (CC BY, © Universität Leipzig / Sächsische Akademie der Wissenschaften / InfAI).
Se [den fullständiga källredovisningen](../tools/glide-dict/ATTRIBUTION.md) och [NOTICE](../NOTICE).

## Teknisk avgränsning

Det befintliga tangentbordet har redan tap positions-avkodning, edit distance, kontextordning,
lokal ordinlärning och flerspråkigt skydd. Jasse bygger vidare på det i `LatinLanguageProvider`.
`SwedishCorrections` lägger till prioriterade, entydiga stavningsrättningar och avgränsad
uppdelning av ihopskrivna fraser. Å, ä och ö behålls som egna bokstäver.

Svenska har produktiv sammansättning: en generell ”två ord finns i ordlistan, dela dem”-regel
skulle skapa särskrivningar. Därför skyddas redan kända ord, ord i andra aktiverade språk och
lokalt inlärda ord. Nya uppdelningar begränsas till vanliga funktionsord i början. Automatisk
uppdelning kräver ett pronomen, belagda ordpar och en tydligt bättre kandidat. Svagare belägg
ger ett förslag att trycka på. `dumärker` ger `du märker`, men ordparet finns inte i den här
korpusen och ersätts därför först när användaren väljer förslaget.

Rättningsstyrkan påverkar ordparströskeln. Båda nya funktionerna har egna inställningar under
Skrivning. Autokorrigering avstängd innebär förslag utan automatisk ersättning. Bara text med
matchande tangenttryckshistorik får ersättas automatiskt av de nya reglerna; diktering och
inklistrad text behöver ett aktivt val.

Hunspell kan i en senare etapp ge bättre stöd för böjningar/sammansättningar, men behöver ett
Android-gränssnitt, licensgranskning och mätning av svarstid/minne. SALDO/SALDOM,
Språkbankens resurser och öppna svenska språkmodeller är också möjliga kandidater, med
separata licenser och storlekskrav som behöver granskas. Ingen sådan modell levereras i denna
testversion. En större modell är inte automatiskt en bättre lösning per tangenttryck.

Regressionstesterna använder de riktiga paketerade datafilerna. De täcker vanliga felstavningar,
transponerade tangenter, saknade mellanslag, korrekta sammansättningar, namn, identifierare,
inställningar och tangenttryckshistorik. Detta är en avgränsad testmängd, ingen mätning som
visar att kvaliteten motsvarar Gboard.
