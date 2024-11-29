package me.clarius.sdk.cast.example.procedure

class ProcedureList() {
    companion object {
        private val procedures: List<Procedure> = listOf(
            Procedure("Transabdominal Plane Block", "Description for Transabdominal Plane Block"),
            Procedure("Brachial Plexus Block", "Description for Brachial Plexus Block"),
            Procedure("Femoral Nerve Block", "Description for Femoral Nerve Block")
        )
        fun getProcedures(): List<Procedure> {
            return procedures
        }
    }
}