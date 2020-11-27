package no.kantega;

import static no.kantega.Pub.Drink.BACARDI_SPECIAL;
import static no.kantega.Pub.Drink.GRANS;
import static no.kantega.Pub.Drink.GT;
import static no.kantega.Pub.Drink.HANSA;
import static no.kantega.Pub.Drink.STRONGBOW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Pub spec")
public class PubPricesTest {

    private Pub pub;

    @BeforeEach
    public void setUp() {
        pub = new Pub();
    }

    @Test
    @DisplayName("When we order one beer, then the price is 74 kr.")
    public void oneBeerTest() {
        int actualPrice = pub.computeCost(HANSA.drinkName(), false, 1);
        assertEquals(74, actualPrice);
    }

    @Test
    @DisplayName("When we order one cider, then the price is 103 kr.")
    public void testCidersAreCostly() {
        int actualPrice = pub.computeCost(GRANS.drinkName(), false, 1);
        assertEquals(103, actualPrice);
    }

    @Test
    @DisplayName("When we order a proper cider, then the price is 110 kr.")
    public void testProperCidersAreEvenMoreExpensive() {
        int actualPrice = pub.computeCost(STRONGBOW.drinkName(), false, 1);
        assertEquals(110, actualPrice);
    }

    @Test
    @DisplayName("When we order a gin and tonic, then the price is 115 kr.")
    public void testACocktail() {
        int actualPrice = pub.computeCost(GT.drinkName(), false, 1);
        assertEquals(115, actualPrice);
    }

    @Test
    @DisplayName("When we order a bacardi special, then the price is 127 kr.")
    public void testBacardiSpecial() {
        int actualPrice = pub.computeCost(BACARDI_SPECIAL.drinkName(), false, 1);
        assertEquals(128, actualPrice);
    }

    @Nested
    @DisplayName("Given a customer who is a student")
    class Students {
        @Test
        @DisplayName("When they order a beer, then they get a discount.")
        public void testStudentsGetADiscountForBeer() {
            int actualPrice = pub.computeCost(HANSA.drinkName(), true, 1);
            assertEquals(67, actualPrice);
        }

        @Test
        @DisplayName("When they order multiple beers, they also get a discount.")
        public void testStudentsGetDiscountsWhenOrderingMoreThanOneBeer() {
            int actualPrice = pub.computeCost(HANSA.drinkName(), true, 2);
            assertEquals(133, actualPrice);
        }

        @Test
        @DisplayName("When they order a cocktail, they do not get a discount.")
        public void testStudentsDoNotGetDiscountsForCocktails() {
            int actualPrice = pub.computeCost(GT.drinkName(), true, 1);
            assertEquals(115, actualPrice);
        }
    }

    @Test
    @DisplayName("When they order a drink which is not on the menu, then they are refused.")
    public void testThatADrinkNotInTheSortimentGivesError() {
        assertThrows(RuntimeException.class, () -> pub.computeCost("sanfranciscosling", false, 1));
    }

    @Nested
    @DisplayName("When they order more than two drinks")
    class MultipleDrinks {
        @Test
        @DisplayName("and the order is for cocktails, then they are refused.")
        public void testCanBuyAtMostTwoDrinksInOneGo() {
            assertThrows(RuntimeException.class, () -> pub.computeCost(BACARDI_SPECIAL.drinkName(), false, 3));
        }

        @Test
        @DisplayName("and the order is for beers, then they are served.")
        public void testCanOrderMoreThanTwoBeers() {
            pub.computeCost(HANSA.drinkName(), false, 5);
        }
    }
}
