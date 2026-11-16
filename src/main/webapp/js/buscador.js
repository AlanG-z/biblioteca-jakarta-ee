/* ============================================================
   Búsqueda en vivo (sin botón): la lista se actualiza a medida
   que el usuario escribe el título/autor o cambia el combo de
   categorías, y también en el listado de préstamos del admin.

   Cómo funciona
   -------------
   1. Escucha los cambios del formulario .buscador.
   2. Espera 300 ms tras la última pulsación (debounce) para no
      disparar un pedido por cada letra.
   3. Pide la MISMA URL por AJAX y reemplaza en la página actual
      sólo los bloques marcados con data-busqueda="...".
   4. El formulario nunca se toca: el foco y el cursor quedan
      donde está el usuario.

   Si el navegador no soporta fetch, el script no hace nada y el
   formulario sigue funcionando con el envío normal (selección
   progresiva): nunca se pierde la búsqueda clásica.
   ============================================================ */
(function () {
    'use strict';

    var ESPERA = 300;      /* ms de pausa tras la última tecla   */
    var pedidos = null;    /* AbortController del pedido activo  */

    function haySoporte() {
        return typeof window.fetch === 'function'
            && typeof window.URLSearchParams === 'function'
            && typeof window.DOMParser === 'function'
            && typeof window.FormData === 'function';
    }

    /* URL de búsqueda: los campos vacíos no viajan en la query. */
    function construirUrl(form) {
        var datos = new FormData(form);
        var params = new URLSearchParams();
        datos.forEach(function (valor, clave) {
            if (valor !== null && String(valor).trim() !== '') {
                params.append(clave, valor);
            }
        });
        var consulta = params.toString();
        return form.action + (consulta ? '?' + consulta : '');
    }

    /* Copia al DOM actual sólo los bloques data-busqueda que
       existan también en la respuesta del servidor. Si un bloque
       no viene (por ejemplo el contador cuando ya no hay filtro),
       se vacía para que no quede desactualizado. */
    function aplicar(html) {
        var nuevo = new DOMParser().parseFromString(html, 'text/html');
        var zonas = document.querySelectorAll('[data-busqueda]');
        for (var i = 0; i < zonas.length; i++) {
            var clave = zonas[i].getAttribute('data-busqueda');
            var origen = nuevo.querySelector('[data-busqueda="' + clave + '"]');
            zonas[i].innerHTML = origen ? origen.innerHTML : '';
        }
    }

    function marcarCargando(estado) {
        var zonas = document.querySelectorAll('[data-busqueda]');
        for (var i = 0; i < zonas.length; i++) {
            zonas[i].classList.toggle('cargando', estado);
            if (estado) {
                zonas[i].setAttribute('aria-busy', 'true');
            } else {
                zonas[i].removeAttribute('aria-busy');
            }
        }
    }

    function buscar(form) {
        var url = construirUrl(form);

        if (pedidos) {
            pedidos.abort();               /* cancela el pedido anterior */
        }
        pedidos = (typeof AbortController === 'function')
                ? new AbortController() : null;

        marcarCargando(true);

        fetch(url, {
            credentials: 'same-origin',
            signal: pedidos ? pedidos.signal : undefined
        })
        .then(function (respuesta) {
            if (!respuesta.ok) {
                throw new Error('HTTP ' + respuesta.status);
            }
            return respuesta.text();
        })
        .then(function (html) {
            aplicar(html);
            if (window.history && window.history.replaceState) {
                window.history.replaceState(null, '', url);
            }
        })
        .catch(function (error) {
            /* Pedido cancelado: no hay nada que hacer. Otro error
               (por ejemplo sin red) conserva los resultados que ya
               estaban en pantalla. */
            if (error && error.name === 'AbortError') {
                return;
            }
        })
        .then(function () {
            marcarCargando(false);
        });
    }

    function preparar(form) {
        var retardo = null;

        function programar() {
            if (retardo) {
                window.clearTimeout(retardo);
            }
            retardo = window.setTimeout(function () {
                retardo = null;
                buscar(form);
            }, ESPERA);
        }

        var textos = form.querySelectorAll('input[type="search"], input[type="text"]');
        for (var i = 0; i < textos.length; i++) {
            textos[i].addEventListener('input', programar);
            /* El botón de borrado del input[type=search] dispara "search" */
            textos[i].addEventListener('search', programar);
        }

        var combos = form.querySelectorAll('select');
        for (var j = 0; j < combos.length; j++) {
            combos[j].addEventListener('change', function () {
                if (retardo) {
                    window.clearTimeout(retardo);
                }
                buscar(form);
            });
        }

        /* Enter o el botón "Buscar" también van por AJAX, para no
           perder el foco; si fetch falla se conserva lo visible. */
        form.addEventListener('submit', function (evento) {
            evento.preventDefault();
            if (retardo) {
                window.clearTimeout(retardo);
            }
            buscar(form);
        });
    }

    function iniciar() {
        if (!haySoporte()) {
            return;                 /* sin JS moderno: envío normal */
        }
        var formularios = document.querySelectorAll('form.buscador');
        for (var i = 0; i < formularios.length; i++) {
            preparar(formularios[i]);
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', iniciar);
    } else {
        iniciar();
    }
})();
