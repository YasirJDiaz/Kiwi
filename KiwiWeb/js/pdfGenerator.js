
// Servicio de Generación de PDF (Replicando diseño Android)
// Requiere: jsPDF y AutoTable

async function generarComprobantePDF(pedido) {
    const { jsPDF } = window.jspdf;
    const doc = new jsPDF();
    const pageWidth = doc.internal.pageSize.getWidth();

    // --- Configuración de Fuentes y Colores ---
    const grisOscuro = "#444444";
    const grisClaro = "#888888";
    const negro = "#000000";

    let y = 20; // Cursor vertical inicial

    // --- 1. CABECERA ---
    // --- 1. CABECERA ---
    // Intentar cargar logo (Asume que está en assets/img/logo_kiwi.png relativo al index.html)
    try {
        // Aseguramos ruta correcta
        const logoImg = await cargarImagen('./assets/img/logo_kiwi.png');
        // addImage(data, format, x, y, w, h)
        doc.addImage(logoImg, 'PNG', 15, 10, 25, 25);
    } catch (e) {
        console.warn("No se pudo cargar el logo para el PDF check path: ./assets/img/logo_kiwi.png", e);
        // Fallback: Si falla, dibujamos un cuadrado placeholder o texto
        // doc.setFillColor(200); doc.rect(15, 10, 25, 25, 'F');
        // doc.text("KIWI", 18, 25);
    }

    // Texto Cabecera (Alineado Derecha)
    doc.setFont("helvetica", "bold");
    doc.setFontSize(14);
    doc.text("KIWI MODA INC.", pageWidth - 15, 18, { align: "right" });

    doc.setFont("helvetica", "normal");
    doc.setFontSize(9);
    doc.setTextColor(grisClaro);

    let yHeader = 24;
    const infoEmpresa = [
        "Zona Libre, Calle 15, Avenida Santa Isabel",
        "Frente al Banco Davivienda",
        "Colón, República de Panamá",
        "Teléfono: 447-3994",
        "Email: Kiwimodainc@gmail.com"
    ];

    infoEmpresa.forEach(linea => {
        doc.text(linea, pageWidth - 15, yHeader, { align: "right" });
        yHeader += 5;
    });

    y = Math.max(y, yHeader) + 5;

    // Separador
    doc.setDrawColor(200);
    doc.line(15, y, pageWidth - 15, y);
    y += 10;

    // --- 2. INFO CLIENTE ---
    doc.setFont("helvetica", "bold");
    doc.setFontSize(11);
    doc.setTextColor(negro);
    doc.text("DETALLES DEL PEDIDO", 15, y);

    y += 3;
    doc.line(15, y, pageWidth - 15, y);
    y += 10;

    doc.setFontSize(9);

    // Columna Izquierda: Datos Cliente
    doc.setFont("helvetica", "bold");
    doc.text("CLIENTE:", 15, y);
    doc.setFont("helvetica", "normal");
    doc.text((pedido.comprador || "").toUpperCase(), 40, y);

    y += 6;
    doc.setFont("helvetica", "bold");
    doc.text("CELULAR:", 15, y);
    doc.setFont("helvetica", "normal");
    doc.text(pedido.celular || "", 40, y);

    // Columna Derecha: Fechas
    const yFecha = y - 6;
    doc.setFont("helvetica", "bold");
    doc.text("FECHA:", pageWidth - 50, yFecha, { align: "right" });
    doc.setFont("helvetica", "normal");
    doc.text(pedido.fecha || "---", pageWidth - 15, yFecha, { align: "right" });

    doc.setFont("helvetica", "bold");
    doc.text("HORA:", pageWidth - 50, y, { align: "right" });
    doc.setFont("helvetica", "normal");
    doc.text(pedido.hora || "---", pageWidth - 15, y, { align: "right" });

    y += 10;

    // --- 3. TABLA DE PRODUCTOS (AutoTable) ---
    const columnas = ["DESCRIPCIÓN", "CANT.", "PRECIO DOC.", "TOTAL"];

    // Preparar filas
    const filas = (pedido.productos || []).map(item => {
        const prod = item.producto || item; // Compatibilidad versiones
        const nombre = (prod.nombre || "Item").toUpperCase();
        const ref = prod.referencia || "";
        const desc = `${nombre}\nRef: ${ref}`; // Multilinea para descripción

        const precioUnit = prod.precio || 0;
        const totalItem = precioUnit * item.cantidad;

        return [
            desc,
            `${item.cantidad} dncs.`,
            `$${precioUnit.toFixed(2)}`,
            `$${totalItem.toFixed(2)}`
        ];
    });

    doc.autoTable({
        startY: y,
        head: [columnas],
        body: filas,
        theme: 'plain', // Minimalista como en Android
        styles: { fontSize: 8, cellPadding: 3, textColor: negro },
        headStyles: { fillColor: [240, 240, 240], fontStyle: 'bold', textColor: negro },
        columnStyles: {
            0: { cellWidth: 'auto' }, // Descripción variable
            1: { cellWidth: 25, halign: 'center' },
            2: { cellWidth: 30, halign: 'right' },
            3: { cellWidth: 30, halign: 'right' }
        },
        margin: { top: 15, right: 15, bottom: 15, left: 15 },
        didParseCell: function (data) {
            // Ajustes opcionales si se requiere
        }
    });

    y = doc.lastAutoTable.finalY + 5;

    // --- 4. TOTALES ---
    doc.line(15, y, pageWidth - 15, y);
    y += 10;

    doc.setFont("helvetica", "bold");
    doc.setFontSize(12);
    doc.text(`TOTAL A PAGAR: $${(pedido.total || 0).toFixed(2)}`, pageWidth - 15, y, { align: "right" });

    y += 20;

    // --- 5. MÉTODOS DE PAGO Y NOTAS ---
    // Verificar espacio disponible, si no, nueva página
    if (y + 100 > doc.internal.pageSize.getHeight()) {
        doc.addPage();
        y = 20;
    }

    // Fondo gris claro para bloque de pago
    const alturaPago = 90;
    doc.setFillColor(248, 248, 248);
    doc.rect(10, y - 5, pageWidth - 20, alturaPago, 'F');

    doc.setFont("helvetica", "bold");
    doc.setFontSize(10);
    doc.text("MÉTODOS DE PAGO", 15, y + 5);

    y += 15;
    doc.setFont("helvetica", "normal");
    doc.setFontSize(8);
    doc.text("Opción 1: Pago en efectivo directamente en el local.", 15, y);
    y += 6;
    doc.text("Opción 2: Transferencia Bancaria a las siguientes cuentas:", 15, y);
    y += 10;

    // BANCOS
    // Banco General
    doc.setFont("helvetica", "bold");
    doc.text("BANCO GENERAL", 15, y);
    doc.setFont("helvetica", "normal");
    doc.text("Mohamad Hamadeh | AHORRO | 04-27-97-152529-7", 15, y + 5);

    y += 15;

    // Banistmo
    doc.setFont("helvetica", "bold");
    doc.text("BANISTMO", 15, y);
    doc.setFont("helvetica", "normal");
    doc.text("Mohamad Hamadeh | AHORRO | 01-14-669-089", 15, y + 5);

    y += 20;

    // NOTAS IMPORTANTES (Rojo)
    doc.setTextColor(200, 0, 0); // Rojo
    doc.setFont("helvetica", "bolditalic");
    doc.text("IMPORTANTE:", 15, y);
    doc.setFont("helvetica", "normal");
    doc.text("Si transfiere desde OTRO BANCO, debe realizarse por ACH EXPRESS.", 45, y);
    y += 5;
    doc.text("De lo contrario el pago no se reflejará de inmediato y no se le podrá entregar la mercancía,", 15, y);
    y += 5;
    doc.text("se le contactará luego reflejarse el monto y entonces podrá retirar su mercancía.", 15, y);

    doc.setTextColor(negro); // Restore black

    // Footer de Despedida
    y += 25;
    doc.setFont("helvetica", "bold");
    doc.setFontSize(11);
    doc.text("¡MUCHAS GRACIAS POR SU COMPRA!", pageWidth / 2, y, { align: "center" });

    doc.setFont("helvetica", "normal");
    doc.setFontSize(7);
    doc.setTextColor(grisClaro);
    doc.text("Este documento es un comprobante de pedido, no es una factura fiscal.", pageWidth / 2, doc.internal.pageSize.getHeight() - 10, { align: "center" });

    // Guardar
    doc.save(`Pedido_Kiwi_${pedido.id || pedido.timestampCreacion?.seconds || "N"}.pdf`);
}

// Helper para cargar imagen desde URL y obtener base64/img data para jsPDF
function cargarImagen(url) {
    return new Promise((resolve, reject) => {
        const img = new Image();
        img.src = url;
        img.onload = () => resolve(img);
        img.onerror = (e) => reject(e);
    });
}
