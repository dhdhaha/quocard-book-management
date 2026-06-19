package com.quocard.bookmanagement.exception

class NotFoundException(message: String) : RuntimeException(message)

class BusinessRuleViolationException(message: String) : RuntimeException(message)
