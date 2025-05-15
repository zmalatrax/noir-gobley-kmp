package dev.gobley.myfirstproject

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// Hardcoded the circuit as a val because easier than loading it from a file...
private const val FIB_JSON_CONTENT = """
{"noir_version":"1.0.0-beta.3+ceaa1986628197bd1170147f6a07f0f98d21030a","hash":9427160933939481479,"abi":{"parameters":[],"return_type":{"abi_type":{"kind":"field"},"visibility":"public"},"error_types":{}},"bytecode":"H4sIAAAAAAAA/6XOQQqAMAwEwBR8UNIkbXLzKy2m/3+CFBQF9eRcFvawbIJLgqezW4/Ef+i+xVhEouYgpobZuymK9mJkpKZbNuYwserdKzoJBw11jjE1m98WeJfg2w7K7TdV9AAAAA==","debug_symbols":"XYxLCoAwDAXvkrUn8Coi0k9aAqEpsRWk9O5+cCFdzhveNPBoa9woBdlhXhqwOFNI0k2tT2CVmCluw3wYJWMZPww1uZ8tZ8bhn1Uc+qr4lF7X134B","file_map":{},"names":["main"],"brillig_names":[]}
"""

private const val FIB_10K_JSON_CONTENT = """
{"noir_version":"1.0.0-beta.3+ceaa1986628197bd1170147f6a07f0f98d21030a","hash":16382588675421763920,"abi":{"parameters":[],"return_type":{"abi_type":{"kind":"field"},"visibility":"public"},"error_types":{}},"bytecode":"H4sIAAAAAAAA/6XOQQqAMAwEwBZ8UGJNzN78StuY/z9BBEVEPTmXhT0sm9Mlp6ezW46kf/i2peE0imlpqNSZVOFhLkZoxA2MFWwxsVTCPBcvJAjrHL2AY/82pHc5fdsAs26aFfQAAAA=","debug_symbols":"XYxLCoAwDAXvkrUn8Coi0k9aAqEpsRWk9O5+cCFdzhveNPBoa9woBdlhXhqwOFNI0k2tT2CVmCluw3wYJWMZPww1uZ8tZ8bhn1Uc+qr4lF7X134B","file_map":{},"names":["main"],"brillig_names":[]}
"""

@Composable
@Preview
fun App() {
    MaterialTheme {
        val scope = rememberCoroutineScope()
        var proof by remember { mutableStateOf<ByteArray?>(null) }
        var isGeneratingProof by remember { mutableStateOf(false) }
        var generationStatus by remember { mutableStateOf<String?>(null) }
        var isVerifyingProof by remember { mutableStateOf(false) }
        var verificationStatus by remember { mutableStateOf<String?>(null) }

        // SRS will be downloaded from Aztec server (netSrs),
        // in production it should be added locally (srs.local, or srs.dat)
        val srsPath: String? = null
        // Public inputs, for fib there are none
        val inputs = emptyList<String>()

        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (!isGeneratingProof) {
                        scope.launch {
                            isGeneratingProof = true
                            generationStatus = "Generating proof..."
                            proof = null
                            verificationStatus = null
                            try {
                                val generatedProof = uniffi.compose_app.generateNoirProof(FIB_10K_JSON_CONTENT, srsPath, inputs)
                                proof = generatedProof
                                generationStatus = "Proof generated successfully (${generatedProof.size} bytes)."
                            } catch (e: Exception) {
                                generationStatus = "Error generating proof: ${e.message}"
                                println("Proof Generation Error: ${e.stackTraceToString()}")
                            } finally {
                                isGeneratingProof = false
                            }
                        }
                    }
                },
                enabled = !isGeneratingProof && !isVerifyingProof
            ) {
                Text(if (isGeneratingProof) "Generating..." else "Generate Noir Proof")
            }

            generationStatus?.let {
                Text(it, style = MaterialTheme.typography.caption, modifier = Modifier.padding(vertical = 4.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (proof != null && !isVerifyingProof) {
                        scope.launch {
                            isVerifyingProof = true
                            verificationStatus = "Verifying proof..."
                            try {
                                val isValid = uniffi.compose_app.verifyNoirProof(FIB_10K_JSON_CONTENT, proof!!)
                                verificationStatus = "Proof verification result: $isValid"
                            } catch (e: Exception) {
                                verificationStatus = "Error verifying proof: ${e.message}"
                                println("Proof Verification Error: ${e.stackTraceToString()}")
                            } finally {
                                isVerifyingProof = false
                            }
                        }
                    }
                },
                enabled = proof != null && !isVerifyingProof && !isGeneratingProof
            ) {
                Text(if (isVerifyingProof) "Verifying..." else "Verify Noir Proof")
            }

            verificationStatus?.let {
                Text(it, style = MaterialTheme.typography.caption, modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}
