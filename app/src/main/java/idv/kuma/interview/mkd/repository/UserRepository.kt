package idv.kuma.interview.mkd.repository

import android.util.Log
import idv.kuma.interview.mkd.data.User
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class UserRepository(okHttpClient: OkHttpClient) : UserRepoProvider {

    private val TAG = UserRepository::class.java.simpleName
    private val userApi: GithubUserApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(okHttpClient)
            .build()
        userApi = retrofit.create(GithubUserApi::class.java)
    }

    override fun fetchUserList(since: Int, perPage:Int, callback: (List<User>) -> Unit) {
        userApi.fetchUsers(since = since, perPage = perPage)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess {
//                Log.d(TAG, "${it.body()}")
                val userList: MutableList<User> = mutableListOf()
                val jsonArray = JSONArray(it.body())
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    userList.add(
                        User(
                            jsonObject.getInt("id"),
                            jsonObject.getString("login"),
                            jsonObject.getString("url"),
                            jsonObject.getString("html_url"),
                            jsonObject.getString("avatar_url"),
                            jsonObject.getBoolean("site_admin")
                        )
                    )
                }
                callback(userList)
            }
            .doOnError {
                Log.d(TAG, "$it")
            }
            .subscribe()
    }

    override fun fetchUserDetail(username: String, callback: (User) -> Unit) {
        userApi.fetchUserDetail(username)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess {
                Log.d(TAG, "${it.body()}")
                val jsonObject = JSONObject(it.body())
                callback(
                    User(
                        jsonObject.getInt("id"),
                        jsonObject.getString("login"),
                        jsonObject.getString("url"),
                        jsonObject.getString("html_url"),
                        jsonObject.getString("avatar_url"),
                        jsonObject.getBoolean("site_admin"),
                        jsonObject.getString("name"),
                        jsonObject.getString("blog"),
                        jsonObject.getString("location")
                    )
                )
            }
            .doOnError {
                Log.d(TAG, "$it")
            }
            .subscribe()
    }


}