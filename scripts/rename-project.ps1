# 文本级换皮辅助：替换 Java 包前缀与 artifact 片段。先 git commit / 备份再跑。
param(
    [Parameter(Mandatory = $true)][string]$NewPackage,
    [Parameter(Mandatory = $true)][string]$NewArtifact
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$OldPackage = "com.omni.scaffolding"
$OldArtifact = "omni-scaffolding"

Write-Host "Root: $Root"
Write-Host "Package: $OldPackage -> $NewPackage"
Write-Host "Artifact: $OldArtifact -> $NewArtifact"
Write-Host "WARNING: Best-effort text replace. Review diff before commit."
$ok = Read-Host "Continue? [y/N]"
if ($ok -ne "y" -and $ok -ne "Y") { exit 0 }

$excludeDirs = @(".git", "node_modules", "target", "dist")
$exts = @("*.java", "*.xml", "*.yml", "*.yaml", "*.md", "*.ftl", "*.ts", "*.vue", "*.properties")

Get-ChildItem -Path $Root -Recurse -File -Include $exts | Where-Object {
    $p = $_.FullName
    -not ($excludeDirs | Where-Object { $p -match [regex]::Escape($_) })
} | ForEach-Object {
    $content = Get-Content -Raw -LiteralPath $_.FullName -ErrorAction SilentlyContinue
    if ($null -eq $content) { return }
    if ($content -notmatch [regex]::Escape($OldPackage) -and $content -notmatch [regex]::Escape($OldArtifact)) {
        return
    }
    $next = $content.Replace($OldPackage, $NewPackage).Replace($OldArtifact, $NewArtifact)
    if ($next -ne $content) {
        Set-Content -LiteralPath $_.FullName -Value $next -NoNewline
        Write-Host "Updated: $($_.FullName)"
    }
}

$OldPath = $OldPackage.Replace(".", [IO.Path]::DirectorySeparatorChar)
$NewPath = $NewPackage.Replace(".", [IO.Path]::DirectorySeparatorChar)

foreach ($mod in @("omni-common", "omni-framework", "omni-modules", "omni-demo", "omni-quartz", "omni-admin")) {
    $src = Join-Path $Root $mod "src"
    if (-not (Test-Path $src)) { continue }
    Get-ChildItem -Path $src -Recurse -Directory -ErrorAction SilentlyContinue |
        Where-Object { $_.FullName.EndsWith($OldPath) } |
        ForEach-Object {
            $dest = Join-Path $_.Parent.FullName $NewPath
            if (Test-Path $dest) {
                Write-Host "Skip existing: $dest"
                return
            }
            New-Item -ItemType Directory -Path $dest -Force | Out-Null
            Get-ChildItem -LiteralPath $_.FullName -Force | Move-Item -Destination $dest -Force
            Write-Host "Moved: $($_.FullName) -> $dest"
        }
}

Write-Host "Done. Next:"
Write-Host "  mvn -s .mvn/settings.xml -pl omni-admin -am compile -DskipTests"
Write-Host "  Review remaining 'omni' names (module folders, Docker) manually."
