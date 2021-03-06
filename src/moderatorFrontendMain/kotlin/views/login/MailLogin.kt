package views.login

import apiBase
import com.studo.campusqr.common.LoginResult
import kotlinext.js.js
import kotlinx.browser.document
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.events.Event
import react.RBuilder
import react.RProps
import react.RState
import react.dom.div
import react.dom.form
import react.setState
import util.Strings
import util.get
import views.common.spacer
import webcore.NetworkManager
import webcore.extensions.inputValue
import webcore.extensions.launch
import webcore.materialUI.textField
import webcore.materialUI.typography
import webcore.materialUI.withStyles
import kotlin.js.json

interface MailLoginProps : RProps {
  var classes: MailLoginClasses
}

interface MailLoginState : RState {
  var email: String
  var password: String
  var networkRequestInProgress: Boolean
  var errorMessage: String?
}

class MailLogin : LoginDetailComponent<MailLoginProps, MailLoginState>() {
  override fun MailLoginState.init() {
    email = ""
    password = ""
    networkRequestInProgress = false
    errorMessage = null
  }

  private fun login() {
    setState { networkRequestInProgress = true }
    launch {
      val response: LoginResult? = NetworkManager.post<String>(
        url = "$apiBase/user/login",
        params = json(
          "email" to state.email,
          "password" to state.password
        ),
        headers = json(
          "csrfToken" to document.querySelector("meta[name='csrfToken']")!!.getAttribute("content")!!
        )
      )?.let { result -> LoginResult.values().find { it.name == result } ?: LoginResult.UNKNOWN_ERROR }

      setState {
        networkRequestInProgress = false
        errorMessage = when (response) {
          LoginResult.LOGIN_FAILED -> Strings.login_wrong_email_and_pw.get()
          LoginResult.LOGIN_SUCCESSFUL -> {
            launch {
              redirectAfterLogin()
            }
            null
          }
          LoginResult.UNKNOWN_ERROR -> Strings.login_unknown_error.get()
          null -> Strings.network_error_description.get()
        }
      }
    }
  }

  override fun RBuilder.render() {
    typography {
      attrs.className = props.classes.description
      attrs.variant = "body1"
      +Strings.login_email_form_body.get()
    }

    form {
      attrs.onSubmitFunction = { event ->
        event.preventDefault()
        event.stopPropagation()
        login()
      }
      div(props.classes.inputWrapper) {
        textField {
          attrs.fullWidth = true
          attrs.label = Strings.email_address.get()
          attrs.onChange = { event: Event ->
            val value = event.inputValue
            setState {
              email = value
              errorMessage = null
            }
          }
          attrs.value = state.email
        }
        spacer()
        textField {
          attrs.fullWidth = true
          attrs.type = "password"
          attrs.label = Strings.login_email_form_pw_label.get()
          attrs.onChange = { event: Event ->
            val value = event.inputValue
            setState {
              password = value
              errorMessage = null
            }
          }
          attrs.value = state.password
        }
        state.errorMessage?.let { errorMessage ->
          typography {
            attrs.variant = "body1"
            attrs.className = props.classes.description
            +errorMessage
          }
        }
        spacer(32)
        renderLoginNavigationButtonsView(
          LoginNavigationButtonsViewProps.Config(
            networkRequestInProgress = state.networkRequestInProgress,
            backEnabled = false,
            nextButtonText = Strings.login_login_button.get(),
            nextButtonDisabled = state.email.isEmpty() || state.password.isEmpty(),
            onNextAction = {
              login()
            }
          )
        )
      }
    }
    div(props.classes.description) {
      typography {
        +Strings.login_forgot_pw_text.get()
      }
    }
  }
}

interface MailLoginClasses {
  // Keep in sync with MailLoginStyle!
  var inputWrapper: String
  var description: String
  var link: String
  var logo: String
}

private val MailLoginStyle = { theme: dynamic ->
  // Keep in sync with MailLoginClasses!
  js {
    inputWrapper = js {
      padding = 16
    }
    description = js {
      textAlign = "center"
      padding = 16
    }
    link = js {
      color = "white"
    }
    logo = js {
      width = 200
      display = "block"
      margin = "auto"
    }
  }
}

private val styled = withStyles<MailLoginProps, MailLogin>(MailLoginStyle)

fun RBuilder.renderMailLogin() = styled {
}
  