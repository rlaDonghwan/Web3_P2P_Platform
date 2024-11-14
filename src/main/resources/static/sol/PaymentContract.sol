// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

contract PaymentContract {
    // 송금 정보를 담을 이벤트
    event EtherSent(address indexed sender, address indexed receiver, uint256 amount, string message);

    // 이더 송금 함수
    function sendEtherTo(address payable receiver, string memory message) public payable {
        require(msg.value > 0, "송금 금액은 0보다 커야 합니다.");
        require(receiver != address(0), "수신자 주소가 유효하지 않습니다.");

        // 수신자에게 이더 송금
        receiver.transfer(msg.value);

        // 송금 정보 기록을 위한 이벤트 발행
        emit EtherSent(msg.sender, receiver, msg.value, message);
    }

    // 현재 컨트랙트에 있는 잔액 조회
    function getBalance() public view returns (uint256) {
        return address(this).balance;
    }
}