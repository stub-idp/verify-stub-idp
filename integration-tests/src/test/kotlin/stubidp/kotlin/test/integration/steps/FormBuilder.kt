package stubidp.kotlin.test.integration.steps

import javax.ws.rs.core.Form

class FormBuilder private constructor() {
    private val form: Form = Form()
    fun withParam(name: String?, value: String?): FormBuilder {
        form.param(name, value)
        return this
    }

    fun build(): Form {
        return form
    }

    companion object {
        fun newForm(): FormBuilder {
            return FormBuilder()
        }
    }

}