package examples.kotlin.inaka.com.adapters

import android.app.Dialog
import android.app.NotificationManager
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.inaka.killertask.KillerTask
import com.mcxiaoke.koi.HASH
import com.mcxiaoke.koi.ext.newNotification
import com.mcxiaoke.koi.ext.newIntent
import com.mcxiaoke.koi.ext.isConnected
import com.mcxiaoke.koi.ext.networkTypeName
import com.mcxiaoke.koi.ext.Bundle
import com.mcxiaoke.koi.ext.showSoftKeyboard
import com.mcxiaoke.koi.ext.hideSoftKeyboard
import com.mcxiaoke.koi.utils.noSdcard
import examples.kotlin.inaka.com.R
import examples.kotlin.inaka.com.activities.BrowseUrlActivity
import examples.kotlin.inaka.com.activities.MainActivity
import examples.kotlin.inaka.com.activities.ShowUserActivity
import examples.kotlin.inaka.com.activities.SlidingTabsActivity
import examples.kotlin.inaka.com.models.User
import kotlinx.android.synthetic.main.view_example_item.view.*
import org.jetbrains.anko.*
import rx.lang.kotlin.fold
import rx.lang.kotlin.observable
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection

internal class ExamplesListAdapter(context: Context, examples: List<String>) : RecyclerView.Adapter<ExamplesListAdapter.ViewHolder>() {

    internal inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView = itemView.textViewExampleTitle
    }

    private val examples: List<String>
    private val context: Context
    private var counter: Int = 0

    init {
        this.examples = examples
        this.context = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamplesListAdapter.ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_example_item, parent, false))
    }

    override fun onBindViewHolder(holder: ExamplesListAdapter.ViewHolder, position: Int) {
        val exampleItemString = examples[position]

        holder.textView.text = exampleItemString

        holder.itemView.setOnClickListener({
            view ->
            when (position) {
                0 -> context.startActivity<SlidingTabsActivity>()
                1 -> openAlertDialog()
                2 -> displaySelector()
                3 -> {
                    counter++
                    rxUsage()
                }
                4 -> makeNewUser()
                5 -> browseURL()
                6 -> share()
                7 -> sendEmail()
                8 -> pickContactForPhoneCall()
                9 -> networkStatus()
                10 -> killerTaskExample()
                11 -> showNotification()
                12 -> showAndHideKeyboard()
                13 -> encryption()
                14 -> deviceStatus()
                else -> {
                    // this is the else statement ...
                }
            }
        })
    }

    override fun getItemCount(): Int {
        return this.examples.size
    }

    private fun openAlertDialog() {
        // Using Anko library
        context.alert("Want to display an example of a toast?", "Test alert dialog") {
            positiveButton("Yes") { context.longToast("This is a toast!") }
            negativeButton("No") { }
        }.show()

        // Kotlin not using Anko library
        /*  AlertDialog.Builder(context).setTitle("Test alert dialog")
                  ?.setMessage("Want to display an example of a toast?")
                  ?.setPositiveButton("Yes", { dialog, which -> context.longToast("This is a toast!") })
                  ?.setNegativeButton("No", { dialog, which -> })
                  ?.create()
                  ?.show()
          */
    }

    private fun makeNewUser() {
        var dialog = Dialog(context)
        dialog.setContentView(R.layout.view_create_user)
        dialog.setTitle("Create user")

        var textName = dialog.findViewById(R.id.editTextUserName) as EditText
        var textAge = dialog.findViewById(R.id.editTextUserAge) as EditText

        var buttonCancel = dialog.findViewById(R.id.buttonCancelUser) as Button
        buttonCancel.setOnClickListener { dialog.dismiss() }

        var buttonShowUser = dialog.findViewById(R.id.buttonShowUser) as Button

        buttonShowUser.setOnClickListener {
            var name = textName.text.toString()
            var ageString = textAge.text.toString()

            var age: Int = 0
            if (!ageString.equals("")) {
                age = ageString.toInt()
            }

            var user = User(mapOf(
                    "name" to name,
                    "age"  to age
            ))

            val bundle = Bundle { putParcelable("user", user) }
            val intent = context.newIntent<ShowUserActivity>(bundle)
            context.startActivity(intent)
        }

        dialog.show()
    }

    private fun rxUsage() {

        observable<String> { subscriber ->

            subscriber.onStart() // start subscriber (optional)

            // receive data
            subscriber.onNext("H")
            subscriber.onNext("el")
            subscriber.onNext("")
            subscriber.onNext("l")
            subscriber.onNext("o")

            if (counter > 1) {
                subscriber.onNext(" again! (" + counter.toString() + " times)")
            }

            subscriber.onCompleted() // finish receiving data

        }.filter { it.isNotEmpty() }
                .fold (StringBuilder()) { sb, e -> sb.append(e) }
                .map { it.toString() }
                .subscribe { result -> context.toast(result) }
    }

    private fun browseURL() {
        context.startActivity<BrowseUrlActivity>()
    }

    private fun displaySelector() {
        val answers = listOf(
                "Those examples are really awesome!",
                "Yes, they are good",
                "Well, it seems that you didn't make so much effort",
                "I don't like this at all",
                "The answer is Japan")
        context.selector("Do you like those examples?", answers) {
            i ->
            when (i) {
                0, 1 -> context.toast("Thanks!")
                2, 3 -> context.toast("It's open source, feel free to collaborate")
                4 -> context.toast("That's the answer I was waiting for")
            }
        }
    }

    private fun share() {
        context.share("Sharing from kotlillon")
    }

    private fun sendEmail() {
        context.email("", "E-mail sent from kotlillon", "Content ...")
    }

    private fun networkStatus() {
        if (context.isConnected()) {
            context.longToast("You are connected to " + context.networkTypeName())
        } else {
            context.vibrator.vibrate(400)
            context.longToast("You are not connected to any network!")
        }
    }

    private fun pickContactForPhoneCall() {
        var intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        (context as MainActivity).startActivityForResult(intent, MainActivity.PICK_CONTACT)
    }

    private fun killerTaskExample() {
        val progressDialog = ProgressDialog(context);
        progressDialog.setMessage("Loading ...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        KillerTask(
                {
                    var connection: URLConnection? = null;

                    try {
                        var url = URL("https://api.github.com/repositories")
                        connection = url.openConnection();
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    var httpConn = connection as HttpURLConnection;
                    httpConn.connectTimeout = 3000;
                    httpConn.readTimeout = 5000;

                    // implicit return
                    "Search public repositories on Github: " + httpConn.responseMessage
                },
                { result: String ->
                    progressDialog.dismiss()
                    context.longToast(result)
                },
                { e: Exception? ->
                    e?.printStackTrace()
                    progressDialog.dismiss()
                    context.toast(e.toString())
                }).go()
    }

    private fun showNotification() {
        val notification = context.newNotification {
            this.setAutoCancel(true)
                    .setContentTitle("kotlillon notification")
                    .setContentText("Here it is an example.")
                    .setSubText("This is a subtitle.")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(null)
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1000, notification)
    }

    private fun showAndHideKeyboard() {
        var dialog = Dialog(context)
        dialog.setContentView(R.layout.view_show_hide_keyboard)
        dialog.setTitle("Show/Hide keyboard")
        dialog.setCancelable(true)

        val exampleEditText = dialog.findViewById(R.id.exampleEditText) as EditText

        val showKeyboardButton = dialog.findViewById(R.id.showKeyboardButton) as Button
        showKeyboardButton.setOnClickListener { exampleEditText.showSoftKeyboard() }

        val hideKeyboardButton = dialog.findViewById(R.id.hideKeyboardButton) as Button
        hideKeyboardButton.setOnClickListener { exampleEditText.hideSoftKeyboard() }

        dialog.show()
    }

    private fun encryption() {
        var dialog = Dialog(context)
        dialog.setContentView(R.layout.view_encryption)
        dialog.setTitle("Encryption")
        dialog.setCancelable(true)

        val textEditText = dialog.findViewById(R.id.textEditText) as EditText
        val encryptionTextView = dialog.findViewById(R.id.encryptionTextView) as TextView

        val md5Button = dialog.findViewById(R.id.md5Button) as Button
        md5Button.setOnClickListener {
            encryptionTextView.text = HASH.md5(textEditText.text.toString())
        }

        val sha1Button = dialog.findViewById(R.id.sha1Button) as Button
        sha1Button.setOnClickListener {
            encryptionTextView.text = HASH.sha1(textEditText.text.toString())
        }

        val sha256Button = dialog.findViewById(R.id.sha256Button) as Button
        sha256Button.setOnClickListener {
            encryptionTextView.text = HASH.sha256(textEditText.text.toString())
        }

        dialog.show()
    }

    private fun deviceStatus() {
        var deviceStatus = "Free space: " + com.mcxiaoke.koi.utils.freeSpace() + " bytes."
        if(noSdcard()){
            deviceStatus += "\nThere is no SDCard!"
        }
        context.longToast(deviceStatus)
    }

}
