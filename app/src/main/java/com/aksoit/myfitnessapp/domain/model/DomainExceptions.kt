package com.aksoit.myfitnessapp.domain.model

/**
 * Hierarquia de exceções de domínio: impede que exceções SQLite vazem para ViewModels (Spec 08 §3).
 */
sealed class DomainException(message: String, cause: Throwable? = null) : Exception(message, cause)

class TemplateNotFoundException(id: Long) : DomainException("Treino $id não encontrado.")

class SessionNotFoundException(id: Long) : DomainException("Sessão $id não encontrada.")

class ActiveHistoryConstraintException(message: String) : DomainException(message)

class InvalidTemplateException(message: String) : DomainException(message)

class XmlValidationException(message: String, cause: Throwable? = null) : DomainException(message, cause)

class PersistenceException(message: String, cause: Throwable? = null) : DomainException(message, cause)
