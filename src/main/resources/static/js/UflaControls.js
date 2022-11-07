
function uploadTemplateFile() {
    return fetch('/api/files', {method:'POST', body:'{ "foo":"Bar" }'}).then( response => response.json()).then(console.log);
}