package routes.organisation

import com.server.extractPrincipalUserId
import com.server.features.organisation.productCategory.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.plugin.scope


fun Route.productCategoryRoutes() {
    route("/categories") {
        createCategory()
        getAllCategories()

        route("/{categoryId}") {
            getCategory()
            updateCategory()
            deleteCategory()
        }
    }
}

private fun Route.getCategory() {
    get {
        val productCategoryRepository = call.scope.get<ProductCategoryRepository>()

        val categoryId = call.parameters["categoryId"] ?: return@get call.respond(HttpStatusCode.BadRequest)

        return@get call.respond(productCategoryRepository.getById(categoryId, "Not found category with this id").mapToDto())
    }
}

private fun Route.getAllCategories() {
    get {
        val organisationId = call.parameters["organisationId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
        val productCategoryRepository = call.scope.get<ProductCategoryRepository>()

        return@get call.respond(productCategoryRepository.getAllByOrganisationId(organisationId).map { it.mapToDto() })
    }
}

private fun Route.createCategory() {
    post("create") {
        val organisationId = call.parameters["organisationId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
        val userId = call.extractPrincipalUserId() ?: return@post call.respond(HttpStatusCode.BadRequest)

        val productCategoryRepository = call.scope.get<ProductCategoryRepository>()
        val request = call.receiveNullable<ProductCategoryRequest>() ?: return@post call.respond(
            HttpStatusCode.BadRequest
        )

        val newCategory = ProductCategory(
            name = request.name,
            organisationId = organisationId,
            createdBy = userId
        )

        if (productCategoryRepository.insert(newCategory, "Category already exists")) {
            return@post call.respond(HttpStatusCode.Created, CreateProductCategoryResponse(newCategory.id.toHexString()))
        }

    }
}

private fun Route.updateCategory() {
    put {
        val categoryId = call.parameters["categoryId"] ?: return@put call.respond(HttpStatusCode.BadRequest)
        val productCategoryRepository = call.scope.get<ProductCategoryRepository>()

        val request = call.receiveNullable<ProductCategoryRequest>() ?: return@put call.respond(
            HttpStatusCode.BadRequest
        )

        if(productCategoryRepository.updateById(categoryId, request)) {
            return@put call.respond(HttpStatusCode.OK)
        }
    }

}

private fun Route.deleteCategory() {
    delete {
        val productCategoryRepository = call.scope.get<ProductCategoryRepository>()
        val categoryId = call.parameters["categoryId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)

        if( productCategoryRepository.deleteById(categoryId)) {
            call.respond(HttpStatusCode.OK)
        }
    }
}
