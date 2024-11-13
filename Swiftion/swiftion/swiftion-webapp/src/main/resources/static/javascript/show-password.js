// inputId is the ID that the input field has
function togglePassword(inputId) {

    const password = document.getElementById(inputId);

    const type = password
        .getAttribute('type') === 'password' ?
        'text' : 'password';

    password.setAttribute('type', type);
}