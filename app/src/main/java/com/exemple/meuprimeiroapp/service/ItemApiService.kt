import com.exemple.meuprimeiroapp.model.Item
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ItemApiService {

    @GET("items")
    suspend fun getItems(): List<Item>

    @GET("items/{id}")
    suspend fun getItem(@Path("id") id: String): Item

    @DELETE("items/{id}")
    suspend fun deleteItem(@Path("id") id: String)

    // Agora enviamos e recebemos o próprio Item, sem o "value"
    @PATCH("items/{id}")
    suspend fun updateItem(@Path("id") id: String, @Body item: Item): Item

    @POST("items")
    suspend fun addItem(@Body item: Item): Item
}