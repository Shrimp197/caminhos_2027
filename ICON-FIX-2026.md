# Correção v0.3.1 — ícone

A build anterior falhava na compilação porque `MainActivity.java` usava
`PackageManager` sem o respetivo import.

Correção:
- adicionado `import android.content.pm.PackageManager;`
- incrementado `versionCode` para 6.

O objetivo funcional mantém-se: usar o novo ícone escolhido como ícone da aplicação.
