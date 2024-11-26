// SPDX-License-Identifier: MIT
pragma solidity ^0.8.17;

contract SafeCommerce2 {
    struct Order {
        address buyer;
        address[] sellers;
        uint[] amounts;
        uint totalAmount;
        bool isPaid;
        bool isCancelled;
    }

    mapping(uint => Order) public orders;
    uint public orderCount;

    event OrderCreated(uint indexed orderId, address indexed buyer, uint totalAmount);
    event FundsDistributed(uint indexed orderId, address indexed seller, uint amount);
    event RefundProcessed(address indexed buyer, uint refundAmount);

    // 주문 생성 및 결제 통합
    function createAndPayOrder(address[] memory sellers, uint[] memory amounts) public payable returns (uint) {
        uint totalAmount = 0;
        uint sellersLength = sellers.length;

        require(sellersLength == amounts.length, "Sellers and amounts length mismatch");

        // 총 금액 계산
        for (uint i = 0; i < sellersLength; i++) {
            require(amounts[i] > 0, "Amount must be greater than zero");
            totalAmount += amounts[i];
        }

        require(msg.value >= totalAmount, "Insufficient payment amount");

        // 주문 생성
        orders[orderCount] = Order({
            buyer: msg.sender,
            sellers: sellers,
            amounts: amounts,
            totalAmount: totalAmount,
            isPaid: true,
            isCancelled: false
        });

        emit OrderCreated(orderCount, msg.sender, totalAmount);

        uint totalSent = 0;

        // 판매자에게 금액 전송
        for (uint i = 0; i < sellersLength; i++) {
            address seller = sellers[i];
            uint amount = amounts[i];

            (bool success, ) = seller.call{value: amount}("");
            if (!success) {
                // 실패한 경우에도 이벤트를 기록
                emit FundsDistributed(orderCount, seller, 0);
            } else {
                totalSent += amount;
                emit FundsDistributed(orderCount, seller, amount);
            }
        }

        // 초과 금액 환불 처리
        uint excessAmount = msg.value - totalSent;
        if (excessAmount > 0) {
            (bool refundSuccess, ) = msg.sender.call{value: excessAmount}("");
            require(refundSuccess, "Refund failed");
            emit RefundProcessed(msg.sender, excessAmount);
        }

        orderCount++;
        return orderCount - 1; // 생성된 주문 ID 반환
    }
}