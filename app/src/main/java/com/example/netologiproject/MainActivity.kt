package com.example.netologiproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.wolfram.alpha.WAEngine
import com.wolfram.alpha.WAPlainText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity() {

    //

    val TAG: String = "MainActivity"

    lateinit var requestInput: TextInputEditText

    lateinit var podsAdapter: SimpleAdapter

    lateinit var progressBar: ProgressBar

    lateinit var waEngine: WAEngine

    val pods = mutableListOf<HashMap<String, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "start of onCreate function")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        initWalframEngine()
    }

    fun initViews() {
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        requestInput = findViewById(R.id.text_input_edit)
        requestInput.setOnEditorActionListener { v, actionId, event -> // метод действие
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                pods.clear() // очистить список ответов
                podsAdapter.notifyDataSetChanged() // встряхнуть адаптер, чтобы пользователь увидел, что при запросе у нас очистились предыдущеи

                val question = requestInput.text.toString() // получаем вопрос из текстового поля с помощью атрибута text
                askWolfram(question) // вызываем метод и передаем туда вопрос
            }
            return@setOnEditorActionListener false // клавиатура будет спрятана
        }

        val podsList: ListView = findViewById(R.id.pods_list)

        podsAdapter = SimpleAdapter(
            applicationContext,
            pods,
            R.layout.item_pod,
            arrayOf("Title", "Content"),
            intArrayOf(R.id.title, R.id.content)
        )
        podsList.adapter = podsAdapter

        val voiceInputButton: FloatingActionButton = findViewById(R.id.voice_input_button)
        voiceInputButton.setOnClickListener {
            Log.d(TAG, "FAB")
        }

        progressBar = findViewById(R.id.progress_bar)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_stop -> {
                Log.d(TAG, "action stop")
                return true
            }
            R.id.action_clear -> {
                requestInput.text?.clear() // при нажатии кнопки action clear очищаем список и текстовое поле
                pods.clear() //очищаем список
                podsAdapter.notifyDataSetChanged() // встяхивае адаптор
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun initWalframEngine() {
        waEngine = WAEngine().apply {
            appID = "PKWU45-7G5KJ8GXT9"
            addFormat("plaintext")
        }
    }

    fun showSnackbar(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_INDEFINITE).apply {
            setAction(android.R.string.ok) {
                dismiss()
            }
            show()
        }
    }
    fun askWolfram(request: String) { // пишем метод запроса в Вольфрам
        progressBar.visibility = View.VISIBLE // метод показывает прогресс бар
        CoroutineScope(Dispatchers.IO).launch { // переходим на IO с помощью корутинов
            val query = waEngine.createQuery().apply { input = request } // создает запрос
            runCatching {
                waEngine.performQuery(query) // запускает блок поиска исключений, в котором выполняется Query
            }.onSuccess { result ->
                withContext(Dispatchers.Main) { // в случае успеха возвращается результат
                    progressBar.visibility = View.GONE // и возвращаемся на главный поток
                    // обработать запрос - ответ
                    if (result.isError) {
                        showSnackbar(result.errorMessage) // прячем прогресс бар и показываем снэк бар с ошибкой
                        return@withContext
                    }
                    if (!result.isSuccess) {
                        requestInput.error = getString(R.string.error_do_not_understand) // или раскрашиваем текстовое поле ввода
                        return@withContext
                    }
/*
если ошибок нет то наполняется список с pods
 */
                    for (pod in result.pods) {
                        if (pod.isError) continue
                        val content = StringBuilder() // строчка, но только большая (макс)
                        for (subpod in pod.subpods) {
                            for (element in subpod.contents) {
                                if (element is WAPlainText) {
                                    content.append(element.text)
                                }
                            }
                        }
                        pods.add(0, HashMap<String, String>().apply {
                            put("Title", pod.title)
                            put("Content", content.toString())
                        })
                    }
                    podsAdapter.notifyDataSetChanged() // после наполнения списка pods встряхиваем адаптер
                }
            }.onFailure { t ->
                withContext(Dispatchers.Main) { // проброс исключения, переключаем на main
                    progressBar.visibility = View.GONE // прячем пргресс бар
                    // обработать ошибку
                    showSnackbar(t.message ?: getString(R.string.error_something_went_wrong)) // показываем сообщение с ошибкой
                }
            }
        }
    }
}





