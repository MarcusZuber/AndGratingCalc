package com.example.gratingcalc

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.lang.Exception
import java.text.NumberFormat
import kotlin.math.pow


enum class Geometry {
    PARALLEL, CONE2G, CONE3G
}

class MainActivity : AppCompatActivity() {
    private lateinit var calculateButton: Button

    private lateinit var g1Pitch: EditText
    private lateinit var g2Pitch: EditText

    private lateinit var geometryGroup: RadioGroup
    private lateinit var parallelGeometry: RadioButton
    private lateinit var cone2g: RadioButton
    private lateinit var cone3g: RadioButton
    private lateinit var result: TextView

    private var geometry = Geometry.PARALLEL


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        calculateButton = findViewById(R.id.calculate)
        geometryGroup = findViewById(R.id.radioGroupGeometry)
        parallelGeometry = findViewById(R.id.parallelBeam)
        cone2g = findViewById(R.id.Cone2G)
        cone3g = findViewById(R.id.Cone3G)

        g1Pitch = findViewById(R.id.g1_pitch)
        g2Pitch = findViewById(R.id.g2_pitch)
        result = findViewById(R.id.result)

        // initialize for parallel geometry
        geometry = Geometry.PARALLEL
        parallelGeometry.isChecked = true
        findViewById<TableRow>(R.id.g2_pitch_row).visibility = View.GONE

        g1Pitch.setText(String.format("%2.2f", 2.4))
        g2Pitch.setText(String.format("%2.2f", 4.8))
        findViewById<EditText>(R.id.energy).setText(String.format("%2.2f", 15.0))
        findViewById<EditText>(R.id.talbot_order).setText("1")

        geometryGroup.setOnCheckedChangeListener{ group, checkedId ->
            val checkedRadioButton = group.findViewById<View>(checkedId) as RadioButton
            val isChecked = checkedRadioButton.isChecked
            if (isChecked) {
                if(checkedRadioButton.id == parallelGeometry.id) {
                    findViewById<TableRow>(R.id.g2_pitch_row).visibility = View.GONE
                    geometry = Geometry.PARALLEL
                    }
                if(checkedRadioButton.id == cone2g.id) {
                    findViewById<TableRow>(R.id.g2_pitch_row).visibility = View.VISIBLE

                    geometry = Geometry.CONE2G
                }
                if(checkedRadioButton.id == cone3g.id) {
                    findViewById<TableRow>(R.id.g2_pitch_row).visibility = View.VISIBLE

                    geometry = Geometry.CONE3G
                }
            }
        }

        calculateButton.setOnClickListener{
            calculateWavelength()
            calculateDistances()
        }

    }

    private fun calculateDistances(){
        val nf = NumberFormat.getInstance()

        val p1Eff = calculateEffectivePeriod()
        val wavelength = calculateWavelength()
        val talbotOrder = findViewById<EditText>(R.id.talbot_order).text.toString().toInt()
        val p2 = nf.parse(findViewById<EditText>(R.id.g2_pitch).text.toString())!!.toFloat() / 1E6
        val g1G2: Double
        val talbotDistance: Double
        val sourceG1: Double
        val g0G1: Double
        val p0: Double

        when (geometry) {
            Geometry.PARALLEL -> {
                talbotDistance = ((talbotOrder * p1Eff.pow(2)) / (2 * wavelength))
                result.text = getString(R.string.G1_G2).format(talbotDistance * 1000)
            }
            Geometry.CONE2G -> {
                g1G2 = ((talbotOrder * p1Eff * p2) / (2 * wavelength))
                sourceG1 = ((g1G2 * p1Eff) / (p2 - p1Eff))
                val resultString = getString(R.string.G1_G2).format(g1G2 * 1000) + "\n" + getString(R.string.source_G1).format(sourceG1 * 1000)
                result.text = resultString
            }
            Geometry.CONE3G -> {
                p0 = p2 * p1Eff / (p2 - p1Eff)
                g1G2 = ((talbotOrder * p1Eff * p2) / (2 * wavelength))
                g0G1 = (p0 / p2 * g1G2)
                val resultString = getString(R.string.G1_G2).format(g1G2 * 1000) + "\n" +  getString(R.string.G0_G1).format(g0G1 * 1000) + "\n" + getString(R.string.p0).format(p0 * 1E6)
                result.text = resultString
            }
        }
    }

    private fun calculateWavelength(): Double {
        val nf = NumberFormat.getInstance()
        val h = 6.62607015E-34 // J s
        val c = 299792458 // m/s
        val q = 1.602176634E-19 // J
        val eInEv = nf.parse(findViewById<EditText>(R.id.energy).text.toString())!!.toDouble() * 1000
        return (h*c) / (eInEv * q)
    }

    private fun calculateEffectivePeriod(): Double {
        val nf = NumberFormat.getInstance()

        val period = nf.parse(findViewById<EditText>(R.id.g1_pitch).text.toString())!!.toFloat() / 1E6
        return when {
            findViewById<RadioButton>(R.id.radio_pi).isChecked -> {
                period / 2
            }
            findViewById<RadioButton>(R.id.radioPiHalf).isChecked -> {
                period
            }
            else -> {
                throw Exception("Phase shift not specified")
            }
        }
    }

}