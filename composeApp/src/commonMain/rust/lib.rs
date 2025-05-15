// This generates extra Rust code required by UniFFI.
uniffi::setup_scaffolding!();

use noir::{
    barretenberg::{
        prove::prove_ultra_honk, srs::setup_srs_from_bytecode, utils::get_honk_verification_key,
        verify::verify_ultra_honk,
    },
    witness::from_vec_str_to_witness_map,
};

// Your NoirBindingError definition from above
#[derive(Debug, thiserror::Error, uniffi::Error)]
pub enum NoirBindingError {
    #[error("Failed to read circuit file: {details}")]
    CircuitFileError { details: String },
    #[error("Failed to parse circuit JSON: {details}")]
    CircuitJsonParseError { details: String },
    #[error("Circuit JSON missing 'bytecode' field or not a string")]
    MissingBytecodeError,
    #[error("SRS setup failed: {details}")]
    SrsSetupError { details: String },
    #[error("Witness creation failed: {details}")]
    WitnessCreationError { details: String },
    #[error("Proof generation failed: {details}")]
    ProofGenerationError { details: String },
    #[error("Verification key generation failed: {details}")]
    VerificationKeyError { details: String },
    #[error("Proof verification failed: {details}")]
    VerificationError { details: String },
    #[error("An internal Noir error occurred: {details}")]
    NoirInternalError { details: String },
}

#[uniffi::export]
pub fn generate_noir_proof(
    circuit_json_content: String,
    srs_path: Option<String>,
    inputs: Vec<String>,
) -> Result<Vec<u8>, NoirBindingError> {
    let circuit_bytecode = get_bytecode(circuit_json_content.clone())?;

    // Setup the SRS
    setup_srs_from_bytecode(circuit_bytecode.as_str(), srs_path.as_deref(), false).map_err(
        |e| NoirBindingError::SrsSetupError {
            details: e.to_string(),
        },
    )?;

    // Set up the witness
    let witness_inputs: Vec<&str> = inputs.iter().map(|s| s.as_str()).collect();
    let witness = from_vec_str_to_witness_map(witness_inputs).map_err(|e| {
        NoirBindingError::WitnessCreationError {
            details: e.to_string(),
        }
    })?;

    prove_ultra_honk(circuit_bytecode.as_str(), witness, false).map_err(|e| {
        NoirBindingError::ProofGenerationError {
            details: e.to_string(),
        }
    })
}

#[uniffi::export]
pub fn verify_noir_proof(
    circuit_json_content: String,
    proof: Vec<u8>,
) -> Result<bool, NoirBindingError> {
    let circuit_bytecode = get_bytecode(circuit_json_content)?;
    let vk = get_honk_verification_key(circuit_bytecode.as_str(), false).map_err(|e| {
        NoirBindingError::VerificationKeyError {
            details: e.to_string(),
        }
    })?;

    verify_ultra_honk(proof, vk).map_err(|e| NoirBindingError::VerificationError {
        details: e.to_string(),
    })
}

// This function is internal, but it's good practice for it to also return a proper Result
// If you intend to expose it via UniFFI later, it will need #[uniffi::export]
// and its error type would also need to be NoirBindingError or another uniffi::Error.
// For now, it's an internal helper.
fn get_bytecode(circuit_json_content: String) -> Result<String, NoirBindingError> {
    // Parse the JSON manifest of the circuit
    let circuit: serde_json::Value = serde_json::from_str(&circuit_json_content).map_err(|e| {
        NoirBindingError::CircuitJsonParseError {
            details: format!("Failed to parse provided JSON circuit content: {}", e),
        }
    })?;

    circuit["bytecode"]
        .as_str()
        .map(|s| s.to_string())
        .ok_or(NoirBindingError::MissingBytecodeError)
}
