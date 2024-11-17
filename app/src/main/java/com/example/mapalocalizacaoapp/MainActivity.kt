package com.example.mapalocalizacaoapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.StreetViewPanoramaView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location? = null
    private lateinit var streetViewPanoramaView: StreetViewPanoramaView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Configura a Toolbar personalizada
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Remove o título padrão da Toolbar
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Cria um TextView para centralizar o título na Toolbar
        val titleTextView = TextView(this).apply {
            text = "Acessibilidade PCD"
            setTextColor(ContextCompat.getColor(context, android.R.color.black))
            textSize = 20f
            layoutParams = Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT
            ).apply { gravity = Gravity.CENTER }
        }
        toolbar.addView(titleTextView)

        // Inicializa o fragmento do mapa
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Inicializa o cliente de localização
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Configura o botão para enviar a localização por e-mail
        val btnEnviarEmail: Button = findViewById(R.id.btnEnviarEmail)
        btnEnviarEmail.setOnClickListener {
            captureMapImage() // Captura a imagem do mapa normal e envia por email
        }

        // Configura o botão para fazer a ligação
        val btnLigar: Button = findViewById(R.id.btnLigar)
        btnLigar.setOnClickListener {
            ligarParaNumero() // Faz a ligação para o número especificado
        }

        // Inicializa o StreetViewPanoramaView
        streetViewPanoramaView = findViewById(R.id.streetViewPanoramaView)
        streetViewPanoramaView.onCreate(savedInstanceState)
    }

    // Método chamado quando o mapa é carregado e pronto para uso
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        atualizarLocalizacao() // Atualiza a localização do usuário no mapa
    }

    // Função que atualiza a localização do usuário no mapa e no StreetView
    private fun atualizarLocalizacao() {
        // Verifica se a permissão para acessar a localização foi concedida
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Obtém a última localização conhecida
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentLocation = location
                    val userLocation = LatLng(location.latitude, location.longitude)

                    // Move a câmera para a localização do usuário
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                    mMap.clear()

                    // Adiciona um marcador no mapa na posição atual do usuário
                    mMap.addMarker(MarkerOptions().position(userLocation).title("Você está aqui"))

                    // Configura o Street View para exibir a localização atual
                    streetViewPanoramaView.getStreetViewPanoramaAsync { panorama ->
                        panorama.setPosition(userLocation)
                    }
                }
            }
        } else {
            // Se a permissão não for concedida, solicita a permissão ao usuário
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        }
    }

    // Função para capturar a imagem do mapa normal como bitmap
    private fun captureMapImage() {
        // Aguardamos o carregamento completo do mapa antes de capturá-lo
        mMap.snapshot { bitmap ->
            if (bitmap != null) {
                // Agora temos a imagem do mapa
                val screenshotUri = saveScreenshot(bitmap)

                // Envia o e-mail com o URI da captura
                enviarEmailComCaptura(screenshotUri)
            }
        }
    }

    // Função para salvar a captura de tela em um arquivo e gerar um URI seguro
    private fun saveScreenshot(bitmap: Bitmap): Uri {
        val file = File(getExternalFilesDir(null), "map_screenshot.png")
        try {
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        // Utiliza FileProvider para criar um URI seguro do arquivo
        return FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
    }

    // Função para enviar um e-mail com a captura de tela do mapa como anexo
    private fun enviarEmailComCaptura(screenshotUri: Uri) {
        currentLocation?.let { location ->
            // Monta a mensagem do e-mail com a localização
            val email = "atendimentosmped@prefeitura.sp.gov.br" // E-mail PCD, SP. Ou Altere para o destinatário desejado
            val assunto = " Solicitação de Melhoria de Acessibilidade PCD nesse local. "
            val mensagem = "Venho por meio deste solicitar melhorias de acessibilidade no local indicado, uma vez que o acesso atual não é adequado para garantir a mobilidade segura e confortável de pessoas com deficiência, idosos e demais cidadãos com dificuldades de locomoção.\n" +
                    "\n" +
                    "A adequação desse espaço é essencial para garantir o direito de todos ao pleno acesso e à participação em atividades no local, conforme previsto pela Lei Brasileira de Inclusão da Pessoa com Deficiência (Lei nº 13.146/2015), que estabelece a obrigação de acessibilidade para todos.\n" +
                    "\n" +
                    "O local é esse informado no link: https://maps.google.com/?q=${location.latitude},${location.longitude}"

            // Cria a intenção para enviar o e-mail
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822" // Tipo de conteúdo para enviar por e-mail
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email)) // Destinatário do e-mail
                putExtra(Intent.EXTRA_SUBJECT, assunto) // Assunto do e-mail
                putExtra(Intent.EXTRA_TEXT, mensagem) // Corpo do e-mail
                putExtra(Intent.EXTRA_STREAM, screenshotUri) // Anexo da captura de tela
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Permissão para o URI
            }

            // Inicia a atividade para enviar o e-mail
            startActivity(Intent.createChooser(intent, "Enviar e-mail..."))
        }
    }

    // Função para iniciar uma ligação para um número específico
    private fun ligarParaNumero() {
        val numeroTelefone = "tel:+551139134000" // Telefone atendimento pessoa PCD SP, ou Substitua pelo número desejado no formato internacional
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse(numeroTelefone)
        }

        // Inicia a intenção para fazer a ligação
        startActivity(intent)
    }

    // Gerenciamento das permissões de localização
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            atualizarLocalizacao() // Se a permissão for concedida, atualiza a localização
        }
    }

    // Libera recursos quando a Activity for parada
    override fun onStop() {
        super.onStop()
        streetViewPanoramaView.onStop() // Libera o StreetViewPanoramaView
    }

    // Inicializa recursos quando a Activity for retomada
    override fun onResume() {
        super.onResume()
        streetViewPanoramaView.onResume() // Inicializa o StreetViewPanoramaView
    }
}
