package dev.fascodes.carRental.listing.specification;

import dev.fascodes.carRental.listing.model.Listing;
import dev.fascodes.carRental.listing.model.ListingStatus;
import org.springframework.data.jpa.domain.Specification;

public class ListingSpecification {

    public static Specification<Listing> isActive() {
        return (root, query, cb) -> cb.equal(root.get("status"), ListingStatus.ACTIVE);
    }

    public static Specification<Listing> priceAtMost(Integer priceMax) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), priceMax);
    }

    public static Specification<Listing> hasBrand(String brand) {
        return (root, query, cb) ->
                cb.equal(cb.lower(root.join("car").get("brand")), brand.toLowerCase());
    }
}
