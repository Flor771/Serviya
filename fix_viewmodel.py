import re

with open('app/src/main/java/com/example/ui/viewmodels/MainViewModel.kt', 'r') as f:
    content = f.read()

# Replace result.onSuccess { created -> 
# with if (result.isSuccess) { val created = result.getOrNull()!!
content = re.sub(r'result\.onSuccess\s*\{\s*([a-zA-Z0-9_]+)\s*->', r'if (result.isSuccess) { val \1 = result.getOrNull()!!', content)

# Replace result.onSuccess { _ ->
# with if (result.isSuccess) {
content = re.sub(r'result\.onSuccess\s*\{\s*_\s*->', r'if (result.isSuccess) {', content)

# Replace result.onSuccess {
# with if (result.isSuccess) { val it = result.getOrNull()!!
# BUT ONLY if it doesn't already have an explicit parameter.
# We will do a generic replacement for the ones that don't have ->
def on_success_replacer(match):
    return 'if (result.isSuccess) { val it = result.getOrNull()!!'

# Find result.onSuccess { (no ->)
content = re.sub(r'result\.onSuccess\s*\{(?![^}]*->)', r'if (result.isSuccess) { val it = result.getOrNull()!!', content)

# Replace }.onFailure {
# with } else { val it = result.exceptionOrNull()!!
content = re.sub(r'\}\.onFailure\s*\{', r'} else { val it = result.exceptionOrNull()!!', content)

with open('app/src/main/java/com/example/ui/viewmodels/MainViewModel.kt', 'w') as f:
    f.write(content)
