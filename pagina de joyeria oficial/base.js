const API_URL = "https://app-6ede98d9-36fc-4dca-8ff2-4a0a19615914.cleverapps.io";
async function login(event) {

    event.preventDefault();

    const correo = document.getElementById("correo").value;

    const password = document.getElementById("password").value;

    try {

        const response = await fetch(`${API_URL}/api/v1/auth/login`, {

            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify({

                correoElectronico: correo,

                password: password

            })

        });

        if (response.ok) {

            const data = await response.json();

            localStorage.setItem("token", data.token);

            alert("Inicio de sesión exitoso");

            window.location.href = "index.html";

        } else {

            alert("Correo o contraseña incorrectos");

        }

    } catch (error) {

        console.error(error);

        alert("Error al conectar con el servidor");

    }

}

     let slides = document.querySelectorAll('.slide');

            let index = 0;

            function changeSlide(){

                slides[index].classList.remove('active');

                index++;

                if(index >= slides.length){
                    index = 0;
                }

                slides[index].classList.add('active');
            }

            /* CAMBIO AUTOMÁTICO */

            setInterval(changeSlide, 4000);