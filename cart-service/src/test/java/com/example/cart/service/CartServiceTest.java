package com.example.cart.service;

import com.example.cart.entity.CartItem;
import com.example.cart.entity.CartItemId;
import com.example.cart.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CartService cartService;

    private CartItem cartItem;
    private CartItemId id;

    @BeforeEach
    void setUp() {
        id = new CartItemId("testUser", 1L);
        cartItem = new CartItem();
        cartItem.setId(id);
        cartItem.setQuantity(2);
        cartItem.setTotalPrice(3.0);
    }

    @Test
    void testAddToCartSuccess() {
        CartService.Product product = new CartService.Product();
        product.setPrice(1.5);
        when(restTemplate.getForObject(anyString(), eq(CartService.Product.class))).thenReturn(product);
        when(cartRepository.save(any(CartItem.class))).thenReturn(cartItem);

        CartItem result = cartService.addToCart(cartItem);
        assertNotNull(result);
        assertEquals(3.0, result.getTotalPrice());
        verify(restTemplate, times(1)).getForObject(anyString(), eq(CartService.Product.class));
        verify(cartRepository, times(1)).save(cartItem);
    }

    @Test
    void testAddToCartProductNotFound() {
        when(restTemplate.getForObject(anyString(), eq(CartService.Product.class)))
                .thenThrow(HttpClientErrorException.create(null, org.springframework.http.HttpStatus.NOT_FOUND, null,
                        null, null, null));
        CartItem result = cartService.addToCart(cartItem);
        assertNull(result);
        verify(cartRepository, never()).save(any());
    }

    @Test
    void testGetCart() {
        when(cartRepository.findByIdUserId("testUser")).thenReturn(List.of(cartItem)); // Fixed method name
        List<CartItem> items = cartService.getCart("testUser");
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(2, items.get(0).getQuantity());
        verify(cartRepository, times(1)).findByIdUserId("testUser"); // Fixed method name
    }

    @Test
    void testRemoveItem() {
        doNothing().when(cartRepository).deleteById(id);
        cartService.removeItem(id);
        verify(cartRepository, times(1)).deleteById(id);
    }

    @Test
    void testUpdateItemSuccess() {
        CartService.Product product = new CartService.Product();
        product.setPrice(1.5);
        when(restTemplate.getForObject(anyString(), eq(CartService.Product.class))).thenReturn(product);
        when(cartRepository.save(any(CartItem.class))).thenReturn(cartItem);
        cartItem.setQuantity(3);

        CartItem result = cartService.updateItem(cartItem);
        assertNotNull(result);
        assertEquals(4.5, result.getTotalPrice());
        verify(restTemplate, times(1)).getForObject(anyString(), eq(CartService.Product.class));
        verify(cartRepository, times(1)).save(cartItem);
    }

    @Test
    void testUpdateItemProductNotFound() {
        when(restTemplate.getForObject(anyString(), eq(CartService.Product.class)))
                .thenThrow(HttpClientErrorException.create(null, org.springframework.http.HttpStatus.NOT_FOUND, null,
                        null, null, null));
        when(cartRepository.findById(id)).thenReturn(Optional.empty()); // Mock findById to return empty

        CartItem result = cartService.updateItem(cartItem);
        assertNull(result);
        verify(restTemplate, times(1)).getForObject(anyString(), eq(CartService.Product.class));
        verify(cartRepository, times(1)).findById(id); // Expect findById to be called
        verify(cartRepository, never()).save(any());
    }

    @Test
    void testClearCart() {
        doNothing().when(cartRepository).deleteCartByUserId("testUser"); // Fixed method name
        cartService.clearCart("testUser");
        verify(cartRepository, times(1)).deleteCartByUserId("testUser"); // Fixed method name
    }
}