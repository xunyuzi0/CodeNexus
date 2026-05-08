$token = "eyJhbGciOiJIUzM4NCJ9.eyJ1c2VyUm9sZSI6ImFkbWluIiwidXNlcklkIjoxNCwic3ViIjoiQ29kZU5leHVzLVVzZXItQXV0aCIsImlhdCI6MTc3ODI3MDcxNiwiZXhwIjoxNzc4ODc1NTE2LCJqdGkiOiI1OTY0NmJjNS02YWIzLTQxMGItOGJmYy1kZWJkMDBmMmNkN2EifQ.5YjNhc-58jZ9u1N95byovW2jjpBObwTWBZ5Loi-U-NBeSaQP98bL8sIc46rUWJUZ"
$headers = @{ "Authorization" = "Bearer $token"; "Content-Type" = "application/json; charset=utf-8" }

$files = Get-ChildItem "F:\Code\A_PROGRAM\CodeNexus\solutions\*.md" | Sort-Object Name

foreach ($file in $files) {
    $raw = Get-Content $file.FullName -Raw -Encoding UTF8

    # parse title
    $titleMatch = [regex]::Match($raw, '(?s)## title\r?\n(.+?)\r?\n\r?\n## content')
    if (-not $titleMatch.Success) { Write-Host "SKIP: $($file.Name) - no title"; continue }
    $title = $titleMatch.Groups[1].Value.Trim()

    # parse content
    $contentMatch = [regex]::Match($raw, '(?s)## content\r?\n(.+?)\r?\n## code')
    if (-not $contentMatch.Success) { Write-Host "SKIP: $($file.Name) - no content"; continue }
    $solContent = $contentMatch.Groups[1].Value.Trim()

    # parse code
    $codeMatch = [regex]::Match($raw, '(?s)## code\r?\n```\w*\r?\n(.+?)```')
    if (-not $codeMatch.Success) { Write-Host "SKIP: $($file.Name) - no code"; continue }
    $code = $codeMatch.Groups[1].Value.Trim()

    # extract problem ID from filename
    $baseName = $file.BaseName
    $idStr = $baseName.Split("-")[0]
    $problemId = [int]$idStr

    $body = @{ title = $title; content = $solContent; code = $code } | ConvertTo-Json -Depth 10

    try {
        $url = "http://localhost:8080/api/admin/problems/$problemId/solutions"
        $r = Invoke-WebRequest -Uri $url -Method Post -Headers $headers -Body ([System.Text.Encoding]::UTF8.GetBytes($body)) -UseBasicParsing
        Write-Host "OK: Problem $problemId ($baseName) -> $($r.StatusCode)"
    } catch {
        $errBody = ""
        if ($_.Exception.Response) {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $errBody = $reader.ReadToEnd()
        }
        Write-Host "FAIL: Problem $problemId ($baseName) -> $($_.Exception.Message) $errBody"
    }
}

Write-Host "Done!"
