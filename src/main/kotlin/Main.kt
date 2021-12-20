import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result;
import com.google.gson.Gson
import java.io.FileWriter

const val RIVELINO_ID = "176665423"
const val TWITTER_URL = "https://api.twitter.com/2/users/$RIVELINO_ID/tweets"

fun main(args: Array<String>) {
    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS") val bearerToken =
        object {}.javaClass.getResource("/bearer.txt").readText()

    val allTweets = mutableListOf<Tweet>()
    var next_token: String? = null
    do {
        val request = makeRequest(bearerToken, next_token)
        val (_, _, result) = request.responseObject<Tweets>()

        val tweets = when (result) {
            is Result.Success -> result.get()
            is Result.Failure -> throw result.getException()
        }

        println(tweets.data)
        allTweets.addAll(tweets.data)
        next_token = tweets.meta.next_token
    } while (next_token != null)

    val gson = Gson()
    val jsonString = gson.toJson(allTweets)
    FileWriter("tweets.json").use { it.write(jsonString) }
}

fun makeRequest(bearerToken: String, next_token: String?): Request {
    val parameters: MutableList<Pair<String, Any?>> =
        if (next_token == null) mutableListOf()
        else mutableListOf("pagination_token" to next_token)
    parameters.add("max_results" to 100)
    parameters.add("exclude" to "retweets")
    val request = TWITTER_URL.httpGet(parameters)
    return request.authentication().bearer(bearerToken)
}

data class Tweet(val id: String, val text: String)

data class Tweets(val data: List<Tweet>, val meta: Meta)

data class Meta(val result_count: Int, val next_token: String?)