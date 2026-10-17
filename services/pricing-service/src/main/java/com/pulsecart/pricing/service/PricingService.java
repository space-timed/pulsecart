package com.pulsecart.pricing.service;

import com.pulsecart.pricing.domain.PriceHistory;
import com.pulsecart.pricing.domain.ProductPrice;
import com.pulsecart.pricing.dto.PricingDtos.*;
import com.pulsecart.pricing.repository.PriceHistoryRepository;
import com.pulsecart.pricing.repository.ProductPriceRepository;
import com.pulsecart.pricing.strategy.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional
public class PricingService {

    private final ProductPriceRepository priceRepository;
    private final PriceHistoryRepository historyRepository;
    private final PricingStrategyResolver strategyResolver;

    public PricingService(ProductPriceRepository priceRepository,
                          PriceHistoryRepository historyRepository,
                          PricingStrategyResolver strategyResolver) {
        this.priceRepository = priceRepository;
        this.historyRepository = historyRepository;
        this.strategyResolver = strategyResolver;
    }

    public PriceResponse setProductPrice(SetPriceRequest request) {
        BigDecimal standardizedPrice = request.basePrice().setScale(4, RoundingMode.HALF_EVEN);
        String currency = request.currency() != null ? request.currency().toUpperCase() : "USD";

        ProductPrice price = priceRepository.findByProductId(request.productId())
                .orElse(null);

        if (price == null) {
            price = new ProductPrice(request.productId(), standardizedPrice, currency);
            priceRepository.save(price);
            historyRepository.save(new PriceHistory(
                    request.productId(),
                    BigDecimal.ZERO,
                    standardizedPrice,
                    currency,
                    request.changedBy() != null ? request.changedBy() : "system",
                    request.reason() != null ? request.reason() : "Initial price assignment"
            ));
        } else {
            BigDecimal oldPrice = price.getBasePrice();
            price.updatePrice(standardizedPrice);
            priceRepository.save(price);
            historyRepository.save(new PriceHistory(
                    request.productId(),
                    oldPrice,
                    standardizedPrice,
                    currency,
                    request.changedBy() != null ? request.changedBy() : "system",
                    request.reason() != null ? request.reason() : "Price adjustment"
            ));
        }

        return toPriceResponse(price);
    }

    @Transactional(readOnly = true)
    public PriceResponse getProductPrice(String productId) {
        ProductPrice price = priceRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("No price found for product ID: " + productId));
        return toPriceResponse(price);
    }

    @Transactional(readOnly = true)
    public PricingCalculationResult calculatePrice(CalculatePriceRequest request) {
        ProductPrice price = priceRepository.findByProductId(request.productId())
                .orElseThrow(() -> new IllegalArgumentException("No price found for product ID: " + request.productId()));

        PricingContext context = new PricingContext(
                request.productId(),
                price.getBasePrice(),
                request.quantity(),
                request.customerTier(),
                request.isFlashSale(),
                request.promoDiscountPercent()
        );

        PricingStrategy strategy = strategyResolver.resolve(context);
        return strategy.calculate(context);
    }

    @Transactional(readOnly = true)
    public List<PriceHistoryResponse> getPriceHistory(String productId) {
        return historyRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    private PriceResponse toPriceResponse(ProductPrice p) {
        return new PriceResponse(p.getId(), p.getProductId(), p.getBasePrice(), p.getCurrency(), p.isActive(), p.getUpdatedAt());
    }

    private PriceHistoryResponse toHistoryResponse(PriceHistory h) {
        return new PriceHistoryResponse(h.getId(), h.getProductId(), h.getOldPrice(), h.getNewPrice(), h.getCurrency(), h.getChangedBy(), h.getReason(), h.getCreatedAt());
    }
}
