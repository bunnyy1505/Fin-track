with open(r"C:\Users\gunde\.gemini\antigravity\scratch\fintrack\src\main\resources\static\index.html", "r", encoding="utf-8") as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if "localStorage" in line:
        print(f"Line {i+1}: {line.strip()}")
