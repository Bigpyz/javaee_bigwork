package com.example.seckill_backend.service.impl;

import com.example.seckill_backend.common.BizException;
import com.example.seckill_backend.common.ErrorCode;
import com.example.seckill_backend.mapper.ProductMapper;
import com.example.seckill_backend.model.Product;
import com.example.seckill_backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商品服务实现类
 * 实现ProductService接口，处理商品的增删改查、秒杀库存扣减/回滚、库存校验等核心业务逻辑
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductMapper productMapper;

    @Override
    public Product createProduct(Product product) {
        validateProductForCreateOrUpdate(product, false);
        if (product.getStatus() == null) {
            product.setStatus(1);
        }
        if (productMapper.insert(product) <= 0) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "创建商品失败");
        }
        return product;
    }

    @Override
    public Product updateProduct(Product product) {
        validateProductForCreateOrUpdate(product, true);
        Product existing = productMapper.selectById(product.getId());
        if (existing == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "商品不存在");
        }
        if (productMapper.update(product) <= 0) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "更新商品失败");
        }
        return productMapper.selectById(product.getId());
    }

    @Override
    public void deleteProduct(Long id) {
        if (id == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "id不能为空");
        }
        Product existing = productMapper.selectById(id);
        if (existing == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "商品不存在");
        }
        if (productMapper.deleteById(id) <= 0) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "删除商品失败");
        }
    }

    @Override
    public Product getProductById(Long id) {
        if (id == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "id不能为空");
        }
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "商品不存在");
        }
        return product;
    }

    @Override
    public List<Product> getAllProducts() {
        return productMapper.selectAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductSeckillStock(Long productId, int quantity) {
        if (productId == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "productId不能为空");
        }
        if (quantity <= 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "quantity必须大于 0");
        }
        Product product = productMapper.selectById(productId);
        if (product == null || product.getSeckillStock() < quantity) {
            return false;
        }
        return productMapper.updateSeckillStock(productId, quantity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean revertSeckillStock(Long productId, int quantity) {
        if (productId == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "productId不能为空");
        }
        if (quantity <= 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "quantity必须大于 0");
        }
        return productMapper.revertSeckillStock(productId, quantity) > 0;
    }

    @Override
    public boolean checkSeckillStock(Long productId, int quantity) {
        if (productId == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "productId不能为空");
        }
        if (quantity <= 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "quantity必须大于 0");
        }
        Product product = productMapper.selectById(productId);
        return product != null && product.getSeckillStock() >= quantity;
    }

    private void validateProductForCreateOrUpdate(Product product, boolean requireId) {
        if (product == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "product不能为空");
        }
        if (requireId && product.getId() == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "id不能为空");
        }
        if (product.getName() == null || product.getName().isBlank()) {
            throw new BizException(ErrorCode.BAD_REQUEST, "name不能为空");
        }
        if (product.getOriginalPrice() == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "originalPrice 不能为空");
        }
        if (product.getSeckillPrice() == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "seckillPrice 不能为空");
        }
        if (product.getTotalStock() == null || product.getTotalStock() < 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "totalStock不合法");
        }
        if (product.getSeckillStock() == null || product.getSeckillStock() < 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "seckillStock不合法");
        }
        if (product.getSeckillStock() > product.getTotalStock()) {
            throw new BizException(ErrorCode.BAD_REQUEST, "seckillStock不能大于totalStock");
        }
    }
}
