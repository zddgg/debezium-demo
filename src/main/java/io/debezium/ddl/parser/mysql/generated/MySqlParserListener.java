package io.debezium.ddl.parser.mysql.generated;

import org.antlr.v4.runtime.tree.ParseTreeListener;

public interface MySqlParserListener extends ParseTreeListener {
    void enterRoot(MySqlParser.RootContext var1);

    void exitRoot(MySqlParser.RootContext var1);

    void enterSqlStatements(MySqlParser.SqlStatementsContext var1);

    void exitSqlStatements(MySqlParser.SqlStatementsContext var1);

    void enterSqlStatement(MySqlParser.SqlStatementContext var1);

    void exitSqlStatement(MySqlParser.SqlStatementContext var1);

    void enterSetStatementFor(MySqlParser.SetStatementForContext var1);

    void exitSetStatementFor(MySqlParser.SetStatementForContext var1);

    void enterEmptyStatement(MySqlParser.EmptyStatementContext var1);

    void exitEmptyStatement(MySqlParser.EmptyStatementContext var1);

    void enterDdlStatement(MySqlParser.DdlStatementContext var1);

    void exitDdlStatement(MySqlParser.DdlStatementContext var1);

    void enterDmlStatement(MySqlParser.DmlStatementContext var1);

    void exitDmlStatement(MySqlParser.DmlStatementContext var1);

    void enterTransactionStatement(MySqlParser.TransactionStatementContext var1);

    void exitTransactionStatement(MySqlParser.TransactionStatementContext var1);

    void enterReplicationStatement(MySqlParser.ReplicationStatementContext var1);

    void exitReplicationStatement(MySqlParser.ReplicationStatementContext var1);

    void enterPreparedStatement(MySqlParser.PreparedStatementContext var1);

    void exitPreparedStatement(MySqlParser.PreparedStatementContext var1);

    void enterCompoundStatement(MySqlParser.CompoundStatementContext var1);

    void exitCompoundStatement(MySqlParser.CompoundStatementContext var1);

    void enterAdministrationStatement(MySqlParser.AdministrationStatementContext var1);

    void exitAdministrationStatement(MySqlParser.AdministrationStatementContext var1);

    void enterUtilityStatement(MySqlParser.UtilityStatementContext var1);

    void exitUtilityStatement(MySqlParser.UtilityStatementContext var1);

    void enterCreateDatabase(MySqlParser.CreateDatabaseContext var1);

    void exitCreateDatabase(MySqlParser.CreateDatabaseContext var1);

    void enterCreateEvent(MySqlParser.CreateEventContext var1);

    void exitCreateEvent(MySqlParser.CreateEventContext var1);

    void enterCreateIndex(MySqlParser.CreateIndexContext var1);

    void exitCreateIndex(MySqlParser.CreateIndexContext var1);

    void enterCreateLogfileGroup(MySqlParser.CreateLogfileGroupContext var1);

    void exitCreateLogfileGroup(MySqlParser.CreateLogfileGroupContext var1);

    void enterCreateProcedure(MySqlParser.CreateProcedureContext var1);

    void exitCreateProcedure(MySqlParser.CreateProcedureContext var1);

    void enterCreateFunction(MySqlParser.CreateFunctionContext var1);

    void exitCreateFunction(MySqlParser.CreateFunctionContext var1);

    void enterCreateRole(MySqlParser.CreateRoleContext var1);

    void exitCreateRole(MySqlParser.CreateRoleContext var1);

    void enterCreateServer(MySqlParser.CreateServerContext var1);

    void exitCreateServer(MySqlParser.CreateServerContext var1);

    void enterCopyCreateTable(MySqlParser.CopyCreateTableContext var1);

    void exitCopyCreateTable(MySqlParser.CopyCreateTableContext var1);

    void enterQueryCreateTable(MySqlParser.QueryCreateTableContext var1);

    void exitQueryCreateTable(MySqlParser.QueryCreateTableContext var1);

    void enterColumnCreateTable(MySqlParser.ColumnCreateTableContext var1);

    void exitColumnCreateTable(MySqlParser.ColumnCreateTableContext var1);

    void enterCreateTablespaceInnodb(MySqlParser.CreateTablespaceInnodbContext var1);

    void exitCreateTablespaceInnodb(MySqlParser.CreateTablespaceInnodbContext var1);

    void enterCreateTablespaceNdb(MySqlParser.CreateTablespaceNdbContext var1);

    void exitCreateTablespaceNdb(MySqlParser.CreateTablespaceNdbContext var1);

    void enterCreateTrigger(MySqlParser.CreateTriggerContext var1);

    void exitCreateTrigger(MySqlParser.CreateTriggerContext var1);

    void enterWithClause(MySqlParser.WithClauseContext var1);

    void exitWithClause(MySqlParser.WithClauseContext var1);

    void enterCommonTableExpressions(MySqlParser.CommonTableExpressionsContext var1);

    void exitCommonTableExpressions(MySqlParser.CommonTableExpressionsContext var1);

    void enterCteName(MySqlParser.CteNameContext var1);

    void exitCteName(MySqlParser.CteNameContext var1);

    void enterCteColumnName(MySqlParser.CteColumnNameContext var1);

    void exitCteColumnName(MySqlParser.CteColumnNameContext var1);

    void enterCreateView(MySqlParser.CreateViewContext var1);

    void exitCreateView(MySqlParser.CreateViewContext var1);

    void enterCreateSequence(MySqlParser.CreateSequenceContext var1);

    void exitCreateSequence(MySqlParser.CreateSequenceContext var1);

    void enterSequenceSpec(MySqlParser.SequenceSpecContext var1);

    void exitSequenceSpec(MySqlParser.SequenceSpecContext var1);

    void enterCreateDatabaseOption(MySqlParser.CreateDatabaseOptionContext var1);

    void exitCreateDatabaseOption(MySqlParser.CreateDatabaseOptionContext var1);

    void enterCharSet(MySqlParser.CharSetContext var1);

    void exitCharSet(MySqlParser.CharSetContext var1);

    void enterOwnerStatement(MySqlParser.OwnerStatementContext var1);

    void exitOwnerStatement(MySqlParser.OwnerStatementContext var1);

    void enterPreciseSchedule(MySqlParser.PreciseScheduleContext var1);

    void exitPreciseSchedule(MySqlParser.PreciseScheduleContext var1);

    void enterIntervalSchedule(MySqlParser.IntervalScheduleContext var1);

    void exitIntervalSchedule(MySqlParser.IntervalScheduleContext var1);

    void enterTimestampValue(MySqlParser.TimestampValueContext var1);

    void exitTimestampValue(MySqlParser.TimestampValueContext var1);

    void enterIntervalExpr(MySqlParser.IntervalExprContext var1);

    void exitIntervalExpr(MySqlParser.IntervalExprContext var1);

    void enterIntervalType(MySqlParser.IntervalTypeContext var1);

    void exitIntervalType(MySqlParser.IntervalTypeContext var1);

    void enterEnableType(MySqlParser.EnableTypeContext var1);

    void exitEnableType(MySqlParser.EnableTypeContext var1);

    void enterIndexType(MySqlParser.IndexTypeContext var1);

    void exitIndexType(MySqlParser.IndexTypeContext var1);

    void enterIndexOption(MySqlParser.IndexOptionContext var1);

    void exitIndexOption(MySqlParser.IndexOptionContext var1);

    void enterProcedureParameter(MySqlParser.ProcedureParameterContext var1);

    void exitProcedureParameter(MySqlParser.ProcedureParameterContext var1);

    void enterFunctionParameter(MySqlParser.FunctionParameterContext var1);

    void exitFunctionParameter(MySqlParser.FunctionParameterContext var1);

    void enterRoutineComment(MySqlParser.RoutineCommentContext var1);

    void exitRoutineComment(MySqlParser.RoutineCommentContext var1);

    void enterRoutineLanguage(MySqlParser.RoutineLanguageContext var1);

    void exitRoutineLanguage(MySqlParser.RoutineLanguageContext var1);

    void enterRoutineBehavior(MySqlParser.RoutineBehaviorContext var1);

    void exitRoutineBehavior(MySqlParser.RoutineBehaviorContext var1);

    void enterRoutineData(MySqlParser.RoutineDataContext var1);

    void exitRoutineData(MySqlParser.RoutineDataContext var1);

    void enterRoutineSecurity(MySqlParser.RoutineSecurityContext var1);

    void exitRoutineSecurity(MySqlParser.RoutineSecurityContext var1);

    void enterServerOption(MySqlParser.ServerOptionContext var1);

    void exitServerOption(MySqlParser.ServerOptionContext var1);

    void enterCreateDefinitions(MySqlParser.CreateDefinitionsContext var1);

    void exitCreateDefinitions(MySqlParser.CreateDefinitionsContext var1);

    void enterColumnDeclaration(MySqlParser.ColumnDeclarationContext var1);

    void exitColumnDeclaration(MySqlParser.ColumnDeclarationContext var1);

    void enterConstraintDeclaration(MySqlParser.ConstraintDeclarationContext var1);

    void exitConstraintDeclaration(MySqlParser.ConstraintDeclarationContext var1);

    void enterIndexDeclaration(MySqlParser.IndexDeclarationContext var1);

    void exitIndexDeclaration(MySqlParser.IndexDeclarationContext var1);

    void enterColumnDefinition(MySqlParser.ColumnDefinitionContext var1);

    void exitColumnDefinition(MySqlParser.ColumnDefinitionContext var1);

    void enterNullColumnConstraint(MySqlParser.NullColumnConstraintContext var1);

    void exitNullColumnConstraint(MySqlParser.NullColumnConstraintContext var1);

    void enterDefaultColumnConstraint(MySqlParser.DefaultColumnConstraintContext var1);

    void exitDefaultColumnConstraint(MySqlParser.DefaultColumnConstraintContext var1);

    void enterVisibilityColumnConstraint(MySqlParser.VisibilityColumnConstraintContext var1);

    void exitVisibilityColumnConstraint(MySqlParser.VisibilityColumnConstraintContext var1);

    void enterInvisibilityColumnConstraint(MySqlParser.InvisibilityColumnConstraintContext var1);

    void exitInvisibilityColumnConstraint(MySqlParser.InvisibilityColumnConstraintContext var1);

    void enterAutoIncrementColumnConstraint(MySqlParser.AutoIncrementColumnConstraintContext var1);

    void exitAutoIncrementColumnConstraint(MySqlParser.AutoIncrementColumnConstraintContext var1);

    void enterPrimaryKeyColumnConstraint(MySqlParser.PrimaryKeyColumnConstraintContext var1);

    void exitPrimaryKeyColumnConstraint(MySqlParser.PrimaryKeyColumnConstraintContext var1);

    void enterClusteringKeyColumnConstraint(MySqlParser.ClusteringKeyColumnConstraintContext var1);

    void exitClusteringKeyColumnConstraint(MySqlParser.ClusteringKeyColumnConstraintContext var1);

    void enterUniqueKeyColumnConstraint(MySqlParser.UniqueKeyColumnConstraintContext var1);

    void exitUniqueKeyColumnConstraint(MySqlParser.UniqueKeyColumnConstraintContext var1);

    void enterCommentColumnConstraint(MySqlParser.CommentColumnConstraintContext var1);

    void exitCommentColumnConstraint(MySqlParser.CommentColumnConstraintContext var1);

    void enterFormatColumnConstraint(MySqlParser.FormatColumnConstraintContext var1);

    void exitFormatColumnConstraint(MySqlParser.FormatColumnConstraintContext var1);

    void enterStorageColumnConstraint(MySqlParser.StorageColumnConstraintContext var1);

    void exitStorageColumnConstraint(MySqlParser.StorageColumnConstraintContext var1);

    void enterReferenceColumnConstraint(MySqlParser.ReferenceColumnConstraintContext var1);

    void exitReferenceColumnConstraint(MySqlParser.ReferenceColumnConstraintContext var1);

    void enterCollateColumnConstraint(MySqlParser.CollateColumnConstraintContext var1);

    void exitCollateColumnConstraint(MySqlParser.CollateColumnConstraintContext var1);

    void enterGeneratedColumnConstraint(MySqlParser.GeneratedColumnConstraintContext var1);

    void exitGeneratedColumnConstraint(MySqlParser.GeneratedColumnConstraintContext var1);

    void enterSerialDefaultColumnConstraint(MySqlParser.SerialDefaultColumnConstraintContext var1);

    void exitSerialDefaultColumnConstraint(MySqlParser.SerialDefaultColumnConstraintContext var1);

    void enterCheckColumnConstraint(MySqlParser.CheckColumnConstraintContext var1);

    void exitCheckColumnConstraint(MySqlParser.CheckColumnConstraintContext var1);

    void enterPrimaryKeyTableConstraint(MySqlParser.PrimaryKeyTableConstraintContext var1);

    void exitPrimaryKeyTableConstraint(MySqlParser.PrimaryKeyTableConstraintContext var1);

    void enterUniqueKeyTableConstraint(MySqlParser.UniqueKeyTableConstraintContext var1);

    void exitUniqueKeyTableConstraint(MySqlParser.UniqueKeyTableConstraintContext var1);

    void enterForeignKeyTableConstraint(MySqlParser.ForeignKeyTableConstraintContext var1);

    void exitForeignKeyTableConstraint(MySqlParser.ForeignKeyTableConstraintContext var1);

    void enterCheckTableConstraint(MySqlParser.CheckTableConstraintContext var1);

    void exitCheckTableConstraint(MySqlParser.CheckTableConstraintContext var1);

    void enterClusteringKeyTableConstraint(MySqlParser.ClusteringKeyTableConstraintContext var1);

    void exitClusteringKeyTableConstraint(MySqlParser.ClusteringKeyTableConstraintContext var1);

    void enterReferenceDefinition(MySqlParser.ReferenceDefinitionContext var1);

    void exitReferenceDefinition(MySqlParser.ReferenceDefinitionContext var1);

    void enterReferenceAction(MySqlParser.ReferenceActionContext var1);

    void exitReferenceAction(MySqlParser.ReferenceActionContext var1);

    void enterReferenceControlType(MySqlParser.ReferenceControlTypeContext var1);

    void exitReferenceControlType(MySqlParser.ReferenceControlTypeContext var1);

    void enterSimpleIndexDeclaration(MySqlParser.SimpleIndexDeclarationContext var1);

    void exitSimpleIndexDeclaration(MySqlParser.SimpleIndexDeclarationContext var1);

    void enterSpecialIndexDeclaration(MySqlParser.SpecialIndexDeclarationContext var1);

    void exitSpecialIndexDeclaration(MySqlParser.SpecialIndexDeclarationContext var1);

    void enterTableOptionEngine(MySqlParser.TableOptionEngineContext var1);

    void exitTableOptionEngine(MySqlParser.TableOptionEngineContext var1);

    void enterTableOptionEngineAttribute(MySqlParser.TableOptionEngineAttributeContext var1);

    void exitTableOptionEngineAttribute(MySqlParser.TableOptionEngineAttributeContext var1);

    void enterTableOptionAutoextendSize(MySqlParser.TableOptionAutoextendSizeContext var1);

    void exitTableOptionAutoextendSize(MySqlParser.TableOptionAutoextendSizeContext var1);

    void enterTableOptionAutoIncrement(MySqlParser.TableOptionAutoIncrementContext var1);

    void exitTableOptionAutoIncrement(MySqlParser.TableOptionAutoIncrementContext var1);

    void enterTableOptionAverage(MySqlParser.TableOptionAverageContext var1);

    void exitTableOptionAverage(MySqlParser.TableOptionAverageContext var1);

    void enterTableOptionCharset(MySqlParser.TableOptionCharsetContext var1);

    void exitTableOptionCharset(MySqlParser.TableOptionCharsetContext var1);

    void enterTableOptionChecksum(MySqlParser.TableOptionChecksumContext var1);

    void exitTableOptionChecksum(MySqlParser.TableOptionChecksumContext var1);

    void enterTableOptionCollate(MySqlParser.TableOptionCollateContext var1);

    void exitTableOptionCollate(MySqlParser.TableOptionCollateContext var1);

    void enterTableOptionComment(MySqlParser.TableOptionCommentContext var1);

    void exitTableOptionComment(MySqlParser.TableOptionCommentContext var1);

    void enterTableOptionCompression(MySqlParser.TableOptionCompressionContext var1);

    void exitTableOptionCompression(MySqlParser.TableOptionCompressionContext var1);

    void enterTableOptionConnection(MySqlParser.TableOptionConnectionContext var1);

    void exitTableOptionConnection(MySqlParser.TableOptionConnectionContext var1);

    void enterTableOptionDataDirectory(MySqlParser.TableOptionDataDirectoryContext var1);

    void exitTableOptionDataDirectory(MySqlParser.TableOptionDataDirectoryContext var1);

    void enterTableOptionDelay(MySqlParser.TableOptionDelayContext var1);

    void exitTableOptionDelay(MySqlParser.TableOptionDelayContext var1);

    void enterTableOptionEncryption(MySqlParser.TableOptionEncryptionContext var1);

    void exitTableOptionEncryption(MySqlParser.TableOptionEncryptionContext var1);

    void enterTableOptionEncrypted(MySqlParser.TableOptionEncryptedContext var1);

    void exitTableOptionEncrypted(MySqlParser.TableOptionEncryptedContext var1);

    void enterTableOptionPageCompressed(MySqlParser.TableOptionPageCompressedContext var1);

    void exitTableOptionPageCompressed(MySqlParser.TableOptionPageCompressedContext var1);

    void enterTableOptionPageCompressionLevel(MySqlParser.TableOptionPageCompressionLevelContext var1);

    void exitTableOptionPageCompressionLevel(MySqlParser.TableOptionPageCompressionLevelContext var1);

    void enterTableOptionEncryptionKeyId(MySqlParser.TableOptionEncryptionKeyIdContext var1);

    void exitTableOptionEncryptionKeyId(MySqlParser.TableOptionEncryptionKeyIdContext var1);

    void enterTableOptionIndexDirectory(MySqlParser.TableOptionIndexDirectoryContext var1);

    void exitTableOptionIndexDirectory(MySqlParser.TableOptionIndexDirectoryContext var1);

    void enterTableOptionInsertMethod(MySqlParser.TableOptionInsertMethodContext var1);

    void exitTableOptionInsertMethod(MySqlParser.TableOptionInsertMethodContext var1);

    void enterTableOptionKeyBlockSize(MySqlParser.TableOptionKeyBlockSizeContext var1);

    void exitTableOptionKeyBlockSize(MySqlParser.TableOptionKeyBlockSizeContext var1);

    void enterTableOptionMaxRows(MySqlParser.TableOptionMaxRowsContext var1);

    void exitTableOptionMaxRows(MySqlParser.TableOptionMaxRowsContext var1);

    void enterTableOptionMinRows(MySqlParser.TableOptionMinRowsContext var1);

    void exitTableOptionMinRows(MySqlParser.TableOptionMinRowsContext var1);

    void enterTableOptionPackKeys(MySqlParser.TableOptionPackKeysContext var1);

    void exitTableOptionPackKeys(MySqlParser.TableOptionPackKeysContext var1);

    void enterTableOptionPassword(MySqlParser.TableOptionPasswordContext var1);

    void exitTableOptionPassword(MySqlParser.TableOptionPasswordContext var1);

    void enterTableOptionRowFormat(MySqlParser.TableOptionRowFormatContext var1);

    void exitTableOptionRowFormat(MySqlParser.TableOptionRowFormatContext var1);

    void enterTableOptionStartTransaction(MySqlParser.TableOptionStartTransactionContext var1);

    void exitTableOptionStartTransaction(MySqlParser.TableOptionStartTransactionContext var1);

    void enterTableOptionSecondaryEngineAttribute(MySqlParser.TableOptionSecondaryEngineAttributeContext var1);

    void exitTableOptionSecondaryEngineAttribute(MySqlParser.TableOptionSecondaryEngineAttributeContext var1);

    void enterTableOptionRecalculation(MySqlParser.TableOptionRecalculationContext var1);

    void exitTableOptionRecalculation(MySqlParser.TableOptionRecalculationContext var1);

    void enterTableOptionPersistent(MySqlParser.TableOptionPersistentContext var1);

    void exitTableOptionPersistent(MySqlParser.TableOptionPersistentContext var1);

    void enterTableOptionSamplePage(MySqlParser.TableOptionSamplePageContext var1);

    void exitTableOptionSamplePage(MySqlParser.TableOptionSamplePageContext var1);

    void enterTableOptionTablespace(MySqlParser.TableOptionTablespaceContext var1);

    void exitTableOptionTablespace(MySqlParser.TableOptionTablespaceContext var1);

    void enterTableOptionTableType(MySqlParser.TableOptionTableTypeContext var1);

    void exitTableOptionTableType(MySqlParser.TableOptionTableTypeContext var1);

    void enterTableOptionTransactional(MySqlParser.TableOptionTransactionalContext var1);

    void exitTableOptionTransactional(MySqlParser.TableOptionTransactionalContext var1);

    void enterTableOptionUnion(MySqlParser.TableOptionUnionContext var1);

    void exitTableOptionUnion(MySqlParser.TableOptionUnionContext var1);

    void enterTableOptionWithSystemVersioning(MySqlParser.TableOptionWithSystemVersioningContext var1);

    void exitTableOptionWithSystemVersioning(MySqlParser.TableOptionWithSystemVersioningContext var1);

    void enterTableType(MySqlParser.TableTypeContext var1);

    void exitTableType(MySqlParser.TableTypeContext var1);

    void enterTablespaceStorage(MySqlParser.TablespaceStorageContext var1);

    void exitTablespaceStorage(MySqlParser.TablespaceStorageContext var1);

    void enterPartitionDefinitions(MySqlParser.PartitionDefinitionsContext var1);

    void exitPartitionDefinitions(MySqlParser.PartitionDefinitionsContext var1);

    void enterPartitionFunctionHash(MySqlParser.PartitionFunctionHashContext var1);

    void exitPartitionFunctionHash(MySqlParser.PartitionFunctionHashContext var1);

    void enterPartitionFunctionKey(MySqlParser.PartitionFunctionKeyContext var1);

    void exitPartitionFunctionKey(MySqlParser.PartitionFunctionKeyContext var1);

    void enterPartitionFunctionRange(MySqlParser.PartitionFunctionRangeContext var1);

    void exitPartitionFunctionRange(MySqlParser.PartitionFunctionRangeContext var1);

    void enterPartitionFunctionList(MySqlParser.PartitionFunctionListContext var1);

    void exitPartitionFunctionList(MySqlParser.PartitionFunctionListContext var1);

    void enterSubPartitionFunctionHash(MySqlParser.SubPartitionFunctionHashContext var1);

    void exitSubPartitionFunctionHash(MySqlParser.SubPartitionFunctionHashContext var1);

    void enterSubPartitionFunctionKey(MySqlParser.SubPartitionFunctionKeyContext var1);

    void exitSubPartitionFunctionKey(MySqlParser.SubPartitionFunctionKeyContext var1);

    void enterPartitionComparison(MySqlParser.PartitionComparisonContext var1);

    void exitPartitionComparison(MySqlParser.PartitionComparisonContext var1);

    void enterPartitionListAtom(MySqlParser.PartitionListAtomContext var1);

    void exitPartitionListAtom(MySqlParser.PartitionListAtomContext var1);

    void enterPartitionListVector(MySqlParser.PartitionListVectorContext var1);

    void exitPartitionListVector(MySqlParser.PartitionListVectorContext var1);

    void enterPartitionSimple(MySqlParser.PartitionSimpleContext var1);

    void exitPartitionSimple(MySqlParser.PartitionSimpleContext var1);

    void enterPartitionDefinerAtom(MySqlParser.PartitionDefinerAtomContext var1);

    void exitPartitionDefinerAtom(MySqlParser.PartitionDefinerAtomContext var1);

    void enterPartitionDefinerVector(MySqlParser.PartitionDefinerVectorContext var1);

    void exitPartitionDefinerVector(MySqlParser.PartitionDefinerVectorContext var1);

    void enterSubpartitionDefinition(MySqlParser.SubpartitionDefinitionContext var1);

    void exitSubpartitionDefinition(MySqlParser.SubpartitionDefinitionContext var1);

    void enterPartitionOptionEngine(MySqlParser.PartitionOptionEngineContext var1);

    void exitPartitionOptionEngine(MySqlParser.PartitionOptionEngineContext var1);

    void enterPartitionOptionComment(MySqlParser.PartitionOptionCommentContext var1);

    void exitPartitionOptionComment(MySqlParser.PartitionOptionCommentContext var1);

    void enterPartitionOptionDataDirectory(MySqlParser.PartitionOptionDataDirectoryContext var1);

    void exitPartitionOptionDataDirectory(MySqlParser.PartitionOptionDataDirectoryContext var1);

    void enterPartitionOptionIndexDirectory(MySqlParser.PartitionOptionIndexDirectoryContext var1);

    void exitPartitionOptionIndexDirectory(MySqlParser.PartitionOptionIndexDirectoryContext var1);

    void enterPartitionOptionMaxRows(MySqlParser.PartitionOptionMaxRowsContext var1);

    void exitPartitionOptionMaxRows(MySqlParser.PartitionOptionMaxRowsContext var1);

    void enterPartitionOptionMinRows(MySqlParser.PartitionOptionMinRowsContext var1);

    void exitPartitionOptionMinRows(MySqlParser.PartitionOptionMinRowsContext var1);

    void enterPartitionOptionTablespace(MySqlParser.PartitionOptionTablespaceContext var1);

    void exitPartitionOptionTablespace(MySqlParser.PartitionOptionTablespaceContext var1);

    void enterPartitionOptionNodeGroup(MySqlParser.PartitionOptionNodeGroupContext var1);

    void exitPartitionOptionNodeGroup(MySqlParser.PartitionOptionNodeGroupContext var1);

    void enterAlterSimpleDatabase(MySqlParser.AlterSimpleDatabaseContext var1);

    void exitAlterSimpleDatabase(MySqlParser.AlterSimpleDatabaseContext var1);

    void enterAlterUpgradeName(MySqlParser.AlterUpgradeNameContext var1);

    void exitAlterUpgradeName(MySqlParser.AlterUpgradeNameContext var1);

    void enterAlterEvent(MySqlParser.AlterEventContext var1);

    void exitAlterEvent(MySqlParser.AlterEventContext var1);

    void enterAlterFunction(MySqlParser.AlterFunctionContext var1);

    void exitAlterFunction(MySqlParser.AlterFunctionContext var1);

    void enterAlterInstance(MySqlParser.AlterInstanceContext var1);

    void exitAlterInstance(MySqlParser.AlterInstanceContext var1);

    void enterAlterLogfileGroup(MySqlParser.AlterLogfileGroupContext var1);

    void exitAlterLogfileGroup(MySqlParser.AlterLogfileGroupContext var1);

    void enterAlterProcedure(MySqlParser.AlterProcedureContext var1);

    void exitAlterProcedure(MySqlParser.AlterProcedureContext var1);

    void enterAlterServer(MySqlParser.AlterServerContext var1);

    void exitAlterServer(MySqlParser.AlterServerContext var1);

    void enterAlterTable(MySqlParser.AlterTableContext var1);

    void exitAlterTable(MySqlParser.AlterTableContext var1);

    void enterAlterTablespace(MySqlParser.AlterTablespaceContext var1);

    void exitAlterTablespace(MySqlParser.AlterTablespaceContext var1);

    void enterAlterView(MySqlParser.AlterViewContext var1);

    void exitAlterView(MySqlParser.AlterViewContext var1);

    void enterAlterSequence(MySqlParser.AlterSequenceContext var1);

    void exitAlterSequence(MySqlParser.AlterSequenceContext var1);

    void enterAlterByTableOption(MySqlParser.AlterByTableOptionContext var1);

    void exitAlterByTableOption(MySqlParser.AlterByTableOptionContext var1);

    void enterAlterByAddColumn(MySqlParser.AlterByAddColumnContext var1);

    void exitAlterByAddColumn(MySqlParser.AlterByAddColumnContext var1);

    void enterAlterByAddColumns(MySqlParser.AlterByAddColumnsContext var1);

    void exitAlterByAddColumns(MySqlParser.AlterByAddColumnsContext var1);

    void enterAlterByAddIndex(MySqlParser.AlterByAddIndexContext var1);

    void exitAlterByAddIndex(MySqlParser.AlterByAddIndexContext var1);

    void enterAlterByAddPrimaryKey(MySqlParser.AlterByAddPrimaryKeyContext var1);

    void exitAlterByAddPrimaryKey(MySqlParser.AlterByAddPrimaryKeyContext var1);

    void enterAlterByAddUniqueKey(MySqlParser.AlterByAddUniqueKeyContext var1);

    void exitAlterByAddUniqueKey(MySqlParser.AlterByAddUniqueKeyContext var1);

    void enterAlterByAddSpecialIndex(MySqlParser.AlterByAddSpecialIndexContext var1);

    void exitAlterByAddSpecialIndex(MySqlParser.AlterByAddSpecialIndexContext var1);

    void enterAlterByAddForeignKey(MySqlParser.AlterByAddForeignKeyContext var1);

    void exitAlterByAddForeignKey(MySqlParser.AlterByAddForeignKeyContext var1);

    void enterAlterByAddCheckTableConstraint(MySqlParser.AlterByAddCheckTableConstraintContext var1);

    void exitAlterByAddCheckTableConstraint(MySqlParser.AlterByAddCheckTableConstraintContext var1);

    void enterAlterByAlterCheckTableConstraint(MySqlParser.AlterByAlterCheckTableConstraintContext var1);

    void exitAlterByAlterCheckTableConstraint(MySqlParser.AlterByAlterCheckTableConstraintContext var1);

    void enterAlterBySetAlgorithm(MySqlParser.AlterBySetAlgorithmContext var1);

    void exitAlterBySetAlgorithm(MySqlParser.AlterBySetAlgorithmContext var1);

    void enterAlterByChangeDefault(MySqlParser.AlterByChangeDefaultContext var1);

    void exitAlterByChangeDefault(MySqlParser.AlterByChangeDefaultContext var1);

    void enterAlterByChangeColumn(MySqlParser.AlterByChangeColumnContext var1);

    void exitAlterByChangeColumn(MySqlParser.AlterByChangeColumnContext var1);

    void enterAlterByRenameColumn(MySqlParser.AlterByRenameColumnContext var1);

    void exitAlterByRenameColumn(MySqlParser.AlterByRenameColumnContext var1);

    void enterAlterByLock(MySqlParser.AlterByLockContext var1);

    void exitAlterByLock(MySqlParser.AlterByLockContext var1);

    void enterAlterByModifyColumn(MySqlParser.AlterByModifyColumnContext var1);

    void exitAlterByModifyColumn(MySqlParser.AlterByModifyColumnContext var1);

    void enterAlterByDropColumn(MySqlParser.AlterByDropColumnContext var1);

    void exitAlterByDropColumn(MySqlParser.AlterByDropColumnContext var1);

    void enterAlterByDropConstraintCheck(MySqlParser.AlterByDropConstraintCheckContext var1);

    void exitAlterByDropConstraintCheck(MySqlParser.AlterByDropConstraintCheckContext var1);

    void enterAlterByDropPrimaryKey(MySqlParser.AlterByDropPrimaryKeyContext var1);

    void exitAlterByDropPrimaryKey(MySqlParser.AlterByDropPrimaryKeyContext var1);

    void enterAlterByDropIndex(MySqlParser.AlterByDropIndexContext var1);

    void exitAlterByDropIndex(MySqlParser.AlterByDropIndexContext var1);

    void enterAlterByRenameIndex(MySqlParser.AlterByRenameIndexContext var1);

    void exitAlterByRenameIndex(MySqlParser.AlterByRenameIndexContext var1);

    void enterAlterByAlterColumnDefault(MySqlParser.AlterByAlterColumnDefaultContext var1);

    void exitAlterByAlterColumnDefault(MySqlParser.AlterByAlterColumnDefaultContext var1);

    void enterAlterByAlterIndexVisibility(MySqlParser.AlterByAlterIndexVisibilityContext var1);

    void exitAlterByAlterIndexVisibility(MySqlParser.AlterByAlterIndexVisibilityContext var1);

    void enterAlterByDropForeignKey(MySqlParser.AlterByDropForeignKeyContext var1);

    void exitAlterByDropForeignKey(MySqlParser.AlterByDropForeignKeyContext var1);

    void enterAlterByDisableKeys(MySqlParser.AlterByDisableKeysContext var1);

    void exitAlterByDisableKeys(MySqlParser.AlterByDisableKeysContext var1);

    void enterAlterByEnableKeys(MySqlParser.AlterByEnableKeysContext var1);

    void exitAlterByEnableKeys(MySqlParser.AlterByEnableKeysContext var1);

    void enterAlterByRename(MySqlParser.AlterByRenameContext var1);

    void exitAlterByRename(MySqlParser.AlterByRenameContext var1);

    void enterAlterByOrder(MySqlParser.AlterByOrderContext var1);

    void exitAlterByOrder(MySqlParser.AlterByOrderContext var1);

    void enterAlterByConvertCharset(MySqlParser.AlterByConvertCharsetContext var1);

    void exitAlterByConvertCharset(MySqlParser.AlterByConvertCharsetContext var1);

    void enterAlterByDefaultCharset(MySqlParser.AlterByDefaultCharsetContext var1);

    void exitAlterByDefaultCharset(MySqlParser.AlterByDefaultCharsetContext var1);

    void enterAlterByDiscardTablespace(MySqlParser.AlterByDiscardTablespaceContext var1);

    void exitAlterByDiscardTablespace(MySqlParser.AlterByDiscardTablespaceContext var1);

    void enterAlterByImportTablespace(MySqlParser.AlterByImportTablespaceContext var1);

    void exitAlterByImportTablespace(MySqlParser.AlterByImportTablespaceContext var1);

    void enterAlterByForce(MySqlParser.AlterByForceContext var1);

    void exitAlterByForce(MySqlParser.AlterByForceContext var1);

    void enterAlterByValidate(MySqlParser.AlterByValidateContext var1);

    void exitAlterByValidate(MySqlParser.AlterByValidateContext var1);

    void enterAlterByAddDefinitions(MySqlParser.AlterByAddDefinitionsContext var1);

    void exitAlterByAddDefinitions(MySqlParser.AlterByAddDefinitionsContext var1);

    void enterAlterPartition(MySqlParser.AlterPartitionContext var1);

    void exitAlterPartition(MySqlParser.AlterPartitionContext var1);

    void enterAlterByAddPartition(MySqlParser.AlterByAddPartitionContext var1);

    void exitAlterByAddPartition(MySqlParser.AlterByAddPartitionContext var1);

    void enterAlterByDropPartition(MySqlParser.AlterByDropPartitionContext var1);

    void exitAlterByDropPartition(MySqlParser.AlterByDropPartitionContext var1);

    void enterAlterByDiscardPartition(MySqlParser.AlterByDiscardPartitionContext var1);

    void exitAlterByDiscardPartition(MySqlParser.AlterByDiscardPartitionContext var1);

    void enterAlterByImportPartition(MySqlParser.AlterByImportPartitionContext var1);

    void exitAlterByImportPartition(MySqlParser.AlterByImportPartitionContext var1);

    void enterAlterByTruncatePartition(MySqlParser.AlterByTruncatePartitionContext var1);

    void exitAlterByTruncatePartition(MySqlParser.AlterByTruncatePartitionContext var1);

    void enterAlterByCoalescePartition(MySqlParser.AlterByCoalescePartitionContext var1);

    void exitAlterByCoalescePartition(MySqlParser.AlterByCoalescePartitionContext var1);

    void enterAlterByReorganizePartition(MySqlParser.AlterByReorganizePartitionContext var1);

    void exitAlterByReorganizePartition(MySqlParser.AlterByReorganizePartitionContext var1);

    void enterAlterByExchangePartition(MySqlParser.AlterByExchangePartitionContext var1);

    void exitAlterByExchangePartition(MySqlParser.AlterByExchangePartitionContext var1);

    void enterAlterByAnalyzePartition(MySqlParser.AlterByAnalyzePartitionContext var1);

    void exitAlterByAnalyzePartition(MySqlParser.AlterByAnalyzePartitionContext var1);

    void enterAlterByCheckPartition(MySqlParser.AlterByCheckPartitionContext var1);

    void exitAlterByCheckPartition(MySqlParser.AlterByCheckPartitionContext var1);

    void enterAlterByOptimizePartition(MySqlParser.AlterByOptimizePartitionContext var1);

    void exitAlterByOptimizePartition(MySqlParser.AlterByOptimizePartitionContext var1);

    void enterAlterByRebuildPartition(MySqlParser.AlterByRebuildPartitionContext var1);

    void exitAlterByRebuildPartition(MySqlParser.AlterByRebuildPartitionContext var1);

    void enterAlterByRepairPartition(MySqlParser.AlterByRepairPartitionContext var1);

    void exitAlterByRepairPartition(MySqlParser.AlterByRepairPartitionContext var1);

    void enterAlterByRemovePartitioning(MySqlParser.AlterByRemovePartitioningContext var1);

    void exitAlterByRemovePartitioning(MySqlParser.AlterByRemovePartitioningContext var1);

    void enterAlterByUpgradePartitioning(MySqlParser.AlterByUpgradePartitioningContext var1);

    void exitAlterByUpgradePartitioning(MySqlParser.AlterByUpgradePartitioningContext var1);

    void enterDropDatabase(MySqlParser.DropDatabaseContext var1);

    void exitDropDatabase(MySqlParser.DropDatabaseContext var1);

    void enterDropEvent(MySqlParser.DropEventContext var1);

    void exitDropEvent(MySqlParser.DropEventContext var1);

    void enterDropIndex(MySqlParser.DropIndexContext var1);

    void exitDropIndex(MySqlParser.DropIndexContext var1);

    void enterDropLogfileGroup(MySqlParser.DropLogfileGroupContext var1);

    void exitDropLogfileGroup(MySqlParser.DropLogfileGroupContext var1);

    void enterDropProcedure(MySqlParser.DropProcedureContext var1);

    void exitDropProcedure(MySqlParser.DropProcedureContext var1);

    void enterDropFunction(MySqlParser.DropFunctionContext var1);

    void exitDropFunction(MySqlParser.DropFunctionContext var1);

    void enterDropServer(MySqlParser.DropServerContext var1);

    void exitDropServer(MySqlParser.DropServerContext var1);

    void enterDropTable(MySqlParser.DropTableContext var1);

    void exitDropTable(MySqlParser.DropTableContext var1);

    void enterDropTablespace(MySqlParser.DropTablespaceContext var1);

    void exitDropTablespace(MySqlParser.DropTablespaceContext var1);

    void enterDropTrigger(MySqlParser.DropTriggerContext var1);

    void exitDropTrigger(MySqlParser.DropTriggerContext var1);

    void enterDropView(MySqlParser.DropViewContext var1);

    void exitDropView(MySqlParser.DropViewContext var1);

    void enterDropRole(MySqlParser.DropRoleContext var1);

    void exitDropRole(MySqlParser.DropRoleContext var1);

    void enterSetRole(MySqlParser.SetRoleContext var1);

    void exitSetRole(MySqlParser.SetRoleContext var1);

    void enterDropSequence(MySqlParser.DropSequenceContext var1);

    void exitDropSequence(MySqlParser.DropSequenceContext var1);

    void enterRenameTable(MySqlParser.RenameTableContext var1);

    void exitRenameTable(MySqlParser.RenameTableContext var1);

    void enterRenameTableClause(MySqlParser.RenameTableClauseContext var1);

    void exitRenameTableClause(MySqlParser.RenameTableClauseContext var1);

    void enterTruncateTable(MySqlParser.TruncateTableContext var1);

    void exitTruncateTable(MySqlParser.TruncateTableContext var1);

    void enterCallStatement(MySqlParser.CallStatementContext var1);

    void exitCallStatement(MySqlParser.CallStatementContext var1);

    void enterDeleteStatement(MySqlParser.DeleteStatementContext var1);

    void exitDeleteStatement(MySqlParser.DeleteStatementContext var1);

    void enterDoStatement(MySqlParser.DoStatementContext var1);

    void exitDoStatement(MySqlParser.DoStatementContext var1);

    void enterHandlerStatement(MySqlParser.HandlerStatementContext var1);

    void exitHandlerStatement(MySqlParser.HandlerStatementContext var1);

    void enterInsertStatement(MySqlParser.InsertStatementContext var1);

    void exitInsertStatement(MySqlParser.InsertStatementContext var1);

    void enterLoadDataStatement(MySqlParser.LoadDataStatementContext var1);

    void exitLoadDataStatement(MySqlParser.LoadDataStatementContext var1);

    void enterLoadXmlStatement(MySqlParser.LoadXmlStatementContext var1);

    void exitLoadXmlStatement(MySqlParser.LoadXmlStatementContext var1);

    void enterReplaceStatement(MySqlParser.ReplaceStatementContext var1);

    void exitReplaceStatement(MySqlParser.ReplaceStatementContext var1);

    void enterSimpleSelect(MySqlParser.SimpleSelectContext var1);

    void exitSimpleSelect(MySqlParser.SimpleSelectContext var1);

    void enterParenthesisSelect(MySqlParser.ParenthesisSelectContext var1);

    void exitParenthesisSelect(MySqlParser.ParenthesisSelectContext var1);

    void enterUnionSelect(MySqlParser.UnionSelectContext var1);

    void exitUnionSelect(MySqlParser.UnionSelectContext var1);

    void enterUnionParenthesisSelect(MySqlParser.UnionParenthesisSelectContext var1);

    void exitUnionParenthesisSelect(MySqlParser.UnionParenthesisSelectContext var1);

    void enterWithLateralStatement(MySqlParser.WithLateralStatementContext var1);

    void exitWithLateralStatement(MySqlParser.WithLateralStatementContext var1);

    void enterValuesStatement(MySqlParser.ValuesStatementContext var1);

    void exitValuesStatement(MySqlParser.ValuesStatementContext var1);

    void enterWithStatement(MySqlParser.WithStatementContext var1);

    void exitWithStatement(MySqlParser.WithStatementContext var1);

    void enterTableStatement(MySqlParser.TableStatementContext var1);

    void exitTableStatement(MySqlParser.TableStatementContext var1);

    void enterUpdateStatement(MySqlParser.UpdateStatementContext var1);

    void exitUpdateStatement(MySqlParser.UpdateStatementContext var1);

    void enterInsertStatementValue(MySqlParser.InsertStatementValueContext var1);

    void exitInsertStatementValue(MySqlParser.InsertStatementValueContext var1);

    void enterUpdatedElement(MySqlParser.UpdatedElementContext var1);

    void exitUpdatedElement(MySqlParser.UpdatedElementContext var1);

    void enterAssignmentField(MySqlParser.AssignmentFieldContext var1);

    void exitAssignmentField(MySqlParser.AssignmentFieldContext var1);

    void enterLockClause(MySqlParser.LockClauseContext var1);

    void exitLockClause(MySqlParser.LockClauseContext var1);

    void enterSingleDeleteStatement(MySqlParser.SingleDeleteStatementContext var1);

    void exitSingleDeleteStatement(MySqlParser.SingleDeleteStatementContext var1);

    void enterMultipleDeleteStatement(MySqlParser.MultipleDeleteStatementContext var1);

    void exitMultipleDeleteStatement(MySqlParser.MultipleDeleteStatementContext var1);

    void enterHandlerOpenStatement(MySqlParser.HandlerOpenStatementContext var1);

    void exitHandlerOpenStatement(MySqlParser.HandlerOpenStatementContext var1);

    void enterHandlerReadIndexStatement(MySqlParser.HandlerReadIndexStatementContext var1);

    void exitHandlerReadIndexStatement(MySqlParser.HandlerReadIndexStatementContext var1);

    void enterHandlerReadStatement(MySqlParser.HandlerReadStatementContext var1);

    void exitHandlerReadStatement(MySqlParser.HandlerReadStatementContext var1);

    void enterHandlerCloseStatement(MySqlParser.HandlerCloseStatementContext var1);

    void exitHandlerCloseStatement(MySqlParser.HandlerCloseStatementContext var1);

    void enterSingleUpdateStatement(MySqlParser.SingleUpdateStatementContext var1);

    void exitSingleUpdateStatement(MySqlParser.SingleUpdateStatementContext var1);

    void enterMultipleUpdateStatement(MySqlParser.MultipleUpdateStatementContext var1);

    void exitMultipleUpdateStatement(MySqlParser.MultipleUpdateStatementContext var1);

    void enterOrderByClause(MySqlParser.OrderByClauseContext var1);

    void exitOrderByClause(MySqlParser.OrderByClauseContext var1);

    void enterOrderByExpression(MySqlParser.OrderByExpressionContext var1);

    void exitOrderByExpression(MySqlParser.OrderByExpressionContext var1);

    void enterTableSources(MySqlParser.TableSourcesContext var1);

    void exitTableSources(MySqlParser.TableSourcesContext var1);

    void enterTableSourceBase(MySqlParser.TableSourceBaseContext var1);

    void exitTableSourceBase(MySqlParser.TableSourceBaseContext var1);

    void enterTableSourceNested(MySqlParser.TableSourceNestedContext var1);

    void exitTableSourceNested(MySqlParser.TableSourceNestedContext var1);

    void enterTableJson(MySqlParser.TableJsonContext var1);

    void exitTableJson(MySqlParser.TableJsonContext var1);

    void enterAtomTableItem(MySqlParser.AtomTableItemContext var1);

    void exitAtomTableItem(MySqlParser.AtomTableItemContext var1);

    void enterSubqueryTableItem(MySqlParser.SubqueryTableItemContext var1);

    void exitSubqueryTableItem(MySqlParser.SubqueryTableItemContext var1);

    void enterTableSourcesItem(MySqlParser.TableSourcesItemContext var1);

    void exitTableSourcesItem(MySqlParser.TableSourcesItemContext var1);

    void enterIndexHint(MySqlParser.IndexHintContext var1);

    void exitIndexHint(MySqlParser.IndexHintContext var1);

    void enterIndexHintType(MySqlParser.IndexHintTypeContext var1);

    void exitIndexHintType(MySqlParser.IndexHintTypeContext var1);

    void enterInnerJoin(MySqlParser.InnerJoinContext var1);

    void exitInnerJoin(MySqlParser.InnerJoinContext var1);

    void enterStraightJoin(MySqlParser.StraightJoinContext var1);

    void exitStraightJoin(MySqlParser.StraightJoinContext var1);

    void enterOuterJoin(MySqlParser.OuterJoinContext var1);

    void exitOuterJoin(MySqlParser.OuterJoinContext var1);

    void enterNaturalJoin(MySqlParser.NaturalJoinContext var1);

    void exitNaturalJoin(MySqlParser.NaturalJoinContext var1);

    void enterQueryExpression(MySqlParser.QueryExpressionContext var1);

    void exitQueryExpression(MySqlParser.QueryExpressionContext var1);

    void enterQueryExpressionNointo(MySqlParser.QueryExpressionNointoContext var1);

    void exitQueryExpressionNointo(MySqlParser.QueryExpressionNointoContext var1);

    void enterQuerySpecification(MySqlParser.QuerySpecificationContext var1);

    void exitQuerySpecification(MySqlParser.QuerySpecificationContext var1);

    void enterQuerySpecificationNointo(MySqlParser.QuerySpecificationNointoContext var1);

    void exitQuerySpecificationNointo(MySqlParser.QuerySpecificationNointoContext var1);

    void enterUnionParenthesis(MySqlParser.UnionParenthesisContext var1);

    void exitUnionParenthesis(MySqlParser.UnionParenthesisContext var1);

    void enterUnionStatement(MySqlParser.UnionStatementContext var1);

    void exitUnionStatement(MySqlParser.UnionStatementContext var1);

    void enterLateralStatement(MySqlParser.LateralStatementContext var1);

    void exitLateralStatement(MySqlParser.LateralStatementContext var1);

    void enterJsonTable(MySqlParser.JsonTableContext var1);

    void exitJsonTable(MySqlParser.JsonTableContext var1);

    void enterJsonColumnList(MySqlParser.JsonColumnListContext var1);

    void exitJsonColumnList(MySqlParser.JsonColumnListContext var1);

    void enterJsonColumn(MySqlParser.JsonColumnContext var1);

    void exitJsonColumn(MySqlParser.JsonColumnContext var1);

    void enterJsonOnEmpty(MySqlParser.JsonOnEmptyContext var1);

    void exitJsonOnEmpty(MySqlParser.JsonOnEmptyContext var1);

    void enterJsonOnError(MySqlParser.JsonOnErrorContext var1);

    void exitJsonOnError(MySqlParser.JsonOnErrorContext var1);

    void enterSelectSpec(MySqlParser.SelectSpecContext var1);

    void exitSelectSpec(MySqlParser.SelectSpecContext var1);

    void enterSelectElements(MySqlParser.SelectElementsContext var1);

    void exitSelectElements(MySqlParser.SelectElementsContext var1);

    void enterSelectStarElement(MySqlParser.SelectStarElementContext var1);

    void exitSelectStarElement(MySqlParser.SelectStarElementContext var1);

    void enterSelectColumnElement(MySqlParser.SelectColumnElementContext var1);

    void exitSelectColumnElement(MySqlParser.SelectColumnElementContext var1);

    void enterSelectFunctionElement(MySqlParser.SelectFunctionElementContext var1);

    void exitSelectFunctionElement(MySqlParser.SelectFunctionElementContext var1);

    void enterSelectExpressionElement(MySqlParser.SelectExpressionElementContext var1);

    void exitSelectExpressionElement(MySqlParser.SelectExpressionElementContext var1);

    void enterSelectIntoVariables(MySqlParser.SelectIntoVariablesContext var1);

    void exitSelectIntoVariables(MySqlParser.SelectIntoVariablesContext var1);

    void enterSelectIntoDumpFile(MySqlParser.SelectIntoDumpFileContext var1);

    void exitSelectIntoDumpFile(MySqlParser.SelectIntoDumpFileContext var1);

    void enterSelectIntoTextFile(MySqlParser.SelectIntoTextFileContext var1);

    void exitSelectIntoTextFile(MySqlParser.SelectIntoTextFileContext var1);

    void enterSelectFieldsInto(MySqlParser.SelectFieldsIntoContext var1);

    void exitSelectFieldsInto(MySqlParser.SelectFieldsIntoContext var1);

    void enterSelectLinesInto(MySqlParser.SelectLinesIntoContext var1);

    void exitSelectLinesInto(MySqlParser.SelectLinesIntoContext var1);

    void enterFromClause(MySqlParser.FromClauseContext var1);

    void exitFromClause(MySqlParser.FromClauseContext var1);

    void enterGroupByClause(MySqlParser.GroupByClauseContext var1);

    void exitGroupByClause(MySqlParser.GroupByClauseContext var1);

    void enterHavingClause(MySqlParser.HavingClauseContext var1);

    void exitHavingClause(MySqlParser.HavingClauseContext var1);

    void enterWindowClause(MySqlParser.WindowClauseContext var1);

    void exitWindowClause(MySqlParser.WindowClauseContext var1);

    void enterGroupByItem(MySqlParser.GroupByItemContext var1);

    void exitGroupByItem(MySqlParser.GroupByItemContext var1);

    void enterLimitClause(MySqlParser.LimitClauseContext var1);

    void exitLimitClause(MySqlParser.LimitClauseContext var1);

    void enterLimitClauseAtom(MySqlParser.LimitClauseAtomContext var1);

    void exitLimitClauseAtom(MySqlParser.LimitClauseAtomContext var1);

    void enterStartTransaction(MySqlParser.StartTransactionContext var1);

    void exitStartTransaction(MySqlParser.StartTransactionContext var1);

    void enterBeginWork(MySqlParser.BeginWorkContext var1);

    void exitBeginWork(MySqlParser.BeginWorkContext var1);

    void enterCommitWork(MySqlParser.CommitWorkContext var1);

    void exitCommitWork(MySqlParser.CommitWorkContext var1);

    void enterRollbackWork(MySqlParser.RollbackWorkContext var1);

    void exitRollbackWork(MySqlParser.RollbackWorkContext var1);

    void enterSavepointStatement(MySqlParser.SavepointStatementContext var1);

    void exitSavepointStatement(MySqlParser.SavepointStatementContext var1);

    void enterRollbackStatement(MySqlParser.RollbackStatementContext var1);

    void exitRollbackStatement(MySqlParser.RollbackStatementContext var1);

    void enterReleaseStatement(MySqlParser.ReleaseStatementContext var1);

    void exitReleaseStatement(MySqlParser.ReleaseStatementContext var1);

    void enterLockTables(MySqlParser.LockTablesContext var1);

    void exitLockTables(MySqlParser.LockTablesContext var1);

    void enterUnlockTables(MySqlParser.UnlockTablesContext var1);

    void exitUnlockTables(MySqlParser.UnlockTablesContext var1);

    void enterSetAutocommitStatement(MySqlParser.SetAutocommitStatementContext var1);

    void exitSetAutocommitStatement(MySqlParser.SetAutocommitStatementContext var1);

    void enterSetTransactionStatement(MySqlParser.SetTransactionStatementContext var1);

    void exitSetTransactionStatement(MySqlParser.SetTransactionStatementContext var1);

    void enterTransactionMode(MySqlParser.TransactionModeContext var1);

    void exitTransactionMode(MySqlParser.TransactionModeContext var1);

    void enterLockTableElement(MySqlParser.LockTableElementContext var1);

    void exitLockTableElement(MySqlParser.LockTableElementContext var1);

    void enterLockAction(MySqlParser.LockActionContext var1);

    void exitLockAction(MySqlParser.LockActionContext var1);

    void enterTransactionOption(MySqlParser.TransactionOptionContext var1);

    void exitTransactionOption(MySqlParser.TransactionOptionContext var1);

    void enterTransactionLevel(MySqlParser.TransactionLevelContext var1);

    void exitTransactionLevel(MySqlParser.TransactionLevelContext var1);

    void enterChangeMaster(MySqlParser.ChangeMasterContext var1);

    void exitChangeMaster(MySqlParser.ChangeMasterContext var1);

    void enterChangeReplicationFilter(MySqlParser.ChangeReplicationFilterContext var1);

    void exitChangeReplicationFilter(MySqlParser.ChangeReplicationFilterContext var1);

    void enterPurgeBinaryLogs(MySqlParser.PurgeBinaryLogsContext var1);

    void exitPurgeBinaryLogs(MySqlParser.PurgeBinaryLogsContext var1);

    void enterResetMaster(MySqlParser.ResetMasterContext var1);

    void exitResetMaster(MySqlParser.ResetMasterContext var1);

    void enterResetSlave(MySqlParser.ResetSlaveContext var1);

    void exitResetSlave(MySqlParser.ResetSlaveContext var1);

    void enterStartSlave(MySqlParser.StartSlaveContext var1);

    void exitStartSlave(MySqlParser.StartSlaveContext var1);

    void enterStopSlave(MySqlParser.StopSlaveContext var1);

    void exitStopSlave(MySqlParser.StopSlaveContext var1);

    void enterStartGroupReplication(MySqlParser.StartGroupReplicationContext var1);

    void exitStartGroupReplication(MySqlParser.StartGroupReplicationContext var1);

    void enterStopGroupReplication(MySqlParser.StopGroupReplicationContext var1);

    void exitStopGroupReplication(MySqlParser.StopGroupReplicationContext var1);

    void enterMasterStringOption(MySqlParser.MasterStringOptionContext var1);

    void exitMasterStringOption(MySqlParser.MasterStringOptionContext var1);

    void enterMasterDecimalOption(MySqlParser.MasterDecimalOptionContext var1);

    void exitMasterDecimalOption(MySqlParser.MasterDecimalOptionContext var1);

    void enterMasterBoolOption(MySqlParser.MasterBoolOptionContext var1);

    void exitMasterBoolOption(MySqlParser.MasterBoolOptionContext var1);

    void enterMasterRealOption(MySqlParser.MasterRealOptionContext var1);

    void exitMasterRealOption(MySqlParser.MasterRealOptionContext var1);

    void enterMasterUidListOption(MySqlParser.MasterUidListOptionContext var1);

    void exitMasterUidListOption(MySqlParser.MasterUidListOptionContext var1);

    void enterStringMasterOption(MySqlParser.StringMasterOptionContext var1);

    void exitStringMasterOption(MySqlParser.StringMasterOptionContext var1);

    void enterDecimalMasterOption(MySqlParser.DecimalMasterOptionContext var1);

    void exitDecimalMasterOption(MySqlParser.DecimalMasterOptionContext var1);

    void enterBoolMasterOption(MySqlParser.BoolMasterOptionContext var1);

    void exitBoolMasterOption(MySqlParser.BoolMasterOptionContext var1);

    void enterChannelOption(MySqlParser.ChannelOptionContext var1);

    void exitChannelOption(MySqlParser.ChannelOptionContext var1);

    void enterDoDbReplication(MySqlParser.DoDbReplicationContext var1);

    void exitDoDbReplication(MySqlParser.DoDbReplicationContext var1);

    void enterIgnoreDbReplication(MySqlParser.IgnoreDbReplicationContext var1);

    void exitIgnoreDbReplication(MySqlParser.IgnoreDbReplicationContext var1);

    void enterDoTableReplication(MySqlParser.DoTableReplicationContext var1);

    void exitDoTableReplication(MySqlParser.DoTableReplicationContext var1);

    void enterIgnoreTableReplication(MySqlParser.IgnoreTableReplicationContext var1);

    void exitIgnoreTableReplication(MySqlParser.IgnoreTableReplicationContext var1);

    void enterWildDoTableReplication(MySqlParser.WildDoTableReplicationContext var1);

    void exitWildDoTableReplication(MySqlParser.WildDoTableReplicationContext var1);

    void enterWildIgnoreTableReplication(MySqlParser.WildIgnoreTableReplicationContext var1);

    void exitWildIgnoreTableReplication(MySqlParser.WildIgnoreTableReplicationContext var1);

    void enterRewriteDbReplication(MySqlParser.RewriteDbReplicationContext var1);

    void exitRewriteDbReplication(MySqlParser.RewriteDbReplicationContext var1);

    void enterTablePair(MySqlParser.TablePairContext var1);

    void exitTablePair(MySqlParser.TablePairContext var1);

    void enterThreadType(MySqlParser.ThreadTypeContext var1);

    void exitThreadType(MySqlParser.ThreadTypeContext var1);

    void enterGtidsUntilOption(MySqlParser.GtidsUntilOptionContext var1);

    void exitGtidsUntilOption(MySqlParser.GtidsUntilOptionContext var1);

    void enterMasterLogUntilOption(MySqlParser.MasterLogUntilOptionContext var1);

    void exitMasterLogUntilOption(MySqlParser.MasterLogUntilOptionContext var1);

    void enterRelayLogUntilOption(MySqlParser.RelayLogUntilOptionContext var1);

    void exitRelayLogUntilOption(MySqlParser.RelayLogUntilOptionContext var1);

    void enterSqlGapsUntilOption(MySqlParser.SqlGapsUntilOptionContext var1);

    void exitSqlGapsUntilOption(MySqlParser.SqlGapsUntilOptionContext var1);

    void enterUserConnectionOption(MySqlParser.UserConnectionOptionContext var1);

    void exitUserConnectionOption(MySqlParser.UserConnectionOptionContext var1);

    void enterPasswordConnectionOption(MySqlParser.PasswordConnectionOptionContext var1);

    void exitPasswordConnectionOption(MySqlParser.PasswordConnectionOptionContext var1);

    void enterDefaultAuthConnectionOption(MySqlParser.DefaultAuthConnectionOptionContext var1);

    void exitDefaultAuthConnectionOption(MySqlParser.DefaultAuthConnectionOptionContext var1);

    void enterPluginDirConnectionOption(MySqlParser.PluginDirConnectionOptionContext var1);

    void exitPluginDirConnectionOption(MySqlParser.PluginDirConnectionOptionContext var1);

    void enterGtuidSet(MySqlParser.GtuidSetContext var1);

    void exitGtuidSet(MySqlParser.GtuidSetContext var1);

    void enterXaStartTransaction(MySqlParser.XaStartTransactionContext var1);

    void exitXaStartTransaction(MySqlParser.XaStartTransactionContext var1);

    void enterXaEndTransaction(MySqlParser.XaEndTransactionContext var1);

    void exitXaEndTransaction(MySqlParser.XaEndTransactionContext var1);

    void enterXaPrepareStatement(MySqlParser.XaPrepareStatementContext var1);

    void exitXaPrepareStatement(MySqlParser.XaPrepareStatementContext var1);

    void enterXaCommitWork(MySqlParser.XaCommitWorkContext var1);

    void exitXaCommitWork(MySqlParser.XaCommitWorkContext var1);

    void enterXaRollbackWork(MySqlParser.XaRollbackWorkContext var1);

    void exitXaRollbackWork(MySqlParser.XaRollbackWorkContext var1);

    void enterXaRecoverWork(MySqlParser.XaRecoverWorkContext var1);

    void exitXaRecoverWork(MySqlParser.XaRecoverWorkContext var1);

    void enterPrepareStatement(MySqlParser.PrepareStatementContext var1);

    void exitPrepareStatement(MySqlParser.PrepareStatementContext var1);

    void enterExecuteStatement(MySqlParser.ExecuteStatementContext var1);

    void exitExecuteStatement(MySqlParser.ExecuteStatementContext var1);

    void enterDeallocatePrepare(MySqlParser.DeallocatePrepareContext var1);

    void exitDeallocatePrepare(MySqlParser.DeallocatePrepareContext var1);

    void enterRoutineBody(MySqlParser.RoutineBodyContext var1);

    void exitRoutineBody(MySqlParser.RoutineBodyContext var1);

    void enterBlockStatement(MySqlParser.BlockStatementContext var1);

    void exitBlockStatement(MySqlParser.BlockStatementContext var1);

    void enterCaseStatement(MySqlParser.CaseStatementContext var1);

    void exitCaseStatement(MySqlParser.CaseStatementContext var1);

    void enterIfStatement(MySqlParser.IfStatementContext var1);

    void exitIfStatement(MySqlParser.IfStatementContext var1);

    void enterIterateStatement(MySqlParser.IterateStatementContext var1);

    void exitIterateStatement(MySqlParser.IterateStatementContext var1);

    void enterLeaveStatement(MySqlParser.LeaveStatementContext var1);

    void exitLeaveStatement(MySqlParser.LeaveStatementContext var1);

    void enterLoopStatement(MySqlParser.LoopStatementContext var1);

    void exitLoopStatement(MySqlParser.LoopStatementContext var1);

    void enterRepeatStatement(MySqlParser.RepeatStatementContext var1);

    void exitRepeatStatement(MySqlParser.RepeatStatementContext var1);

    void enterReturnStatement(MySqlParser.ReturnStatementContext var1);

    void exitReturnStatement(MySqlParser.ReturnStatementContext var1);

    void enterWhileStatement(MySqlParser.WhileStatementContext var1);

    void exitWhileStatement(MySqlParser.WhileStatementContext var1);

    void enterCloseCursor(MySqlParser.CloseCursorContext var1);

    void exitCloseCursor(MySqlParser.CloseCursorContext var1);

    void enterFetchCursor(MySqlParser.FetchCursorContext var1);

    void exitFetchCursor(MySqlParser.FetchCursorContext var1);

    void enterOpenCursor(MySqlParser.OpenCursorContext var1);

    void exitOpenCursor(MySqlParser.OpenCursorContext var1);

    void enterDeclareVariable(MySqlParser.DeclareVariableContext var1);

    void exitDeclareVariable(MySqlParser.DeclareVariableContext var1);

    void enterDeclareCondition(MySqlParser.DeclareConditionContext var1);

    void exitDeclareCondition(MySqlParser.DeclareConditionContext var1);

    void enterDeclareCursor(MySqlParser.DeclareCursorContext var1);

    void exitDeclareCursor(MySqlParser.DeclareCursorContext var1);

    void enterDeclareHandler(MySqlParser.DeclareHandlerContext var1);

    void exitDeclareHandler(MySqlParser.DeclareHandlerContext var1);

    void enterHandlerConditionCode(MySqlParser.HandlerConditionCodeContext var1);

    void exitHandlerConditionCode(MySqlParser.HandlerConditionCodeContext var1);

    void enterHandlerConditionState(MySqlParser.HandlerConditionStateContext var1);

    void exitHandlerConditionState(MySqlParser.HandlerConditionStateContext var1);

    void enterHandlerConditionName(MySqlParser.HandlerConditionNameContext var1);

    void exitHandlerConditionName(MySqlParser.HandlerConditionNameContext var1);

    void enterHandlerConditionWarning(MySqlParser.HandlerConditionWarningContext var1);

    void exitHandlerConditionWarning(MySqlParser.HandlerConditionWarningContext var1);

    void enterHandlerConditionNotfound(MySqlParser.HandlerConditionNotfoundContext var1);

    void exitHandlerConditionNotfound(MySqlParser.HandlerConditionNotfoundContext var1);

    void enterHandlerConditionException(MySqlParser.HandlerConditionExceptionContext var1);

    void exitHandlerConditionException(MySqlParser.HandlerConditionExceptionContext var1);

    void enterProcedureSqlStatement(MySqlParser.ProcedureSqlStatementContext var1);

    void exitProcedureSqlStatement(MySqlParser.ProcedureSqlStatementContext var1);

    void enterCaseAlternative(MySqlParser.CaseAlternativeContext var1);

    void exitCaseAlternative(MySqlParser.CaseAlternativeContext var1);

    void enterElifAlternative(MySqlParser.ElifAlternativeContext var1);

    void exitElifAlternative(MySqlParser.ElifAlternativeContext var1);

    void enterAlterUserMysqlV56(MySqlParser.AlterUserMysqlV56Context var1);

    void exitAlterUserMysqlV56(MySqlParser.AlterUserMysqlV56Context var1);

    void enterAlterUserMysqlV80(MySqlParser.AlterUserMysqlV80Context var1);

    void exitAlterUserMysqlV80(MySqlParser.AlterUserMysqlV80Context var1);

    void enterCreateUserMysqlV56(MySqlParser.CreateUserMysqlV56Context var1);

    void exitCreateUserMysqlV56(MySqlParser.CreateUserMysqlV56Context var1);

    void enterCreateUserMysqlV80(MySqlParser.CreateUserMysqlV80Context var1);

    void exitCreateUserMysqlV80(MySqlParser.CreateUserMysqlV80Context var1);

    void enterDropUser(MySqlParser.DropUserContext var1);

    void exitDropUser(MySqlParser.DropUserContext var1);

    void enterGrantStatement(MySqlParser.GrantStatementContext var1);

    void exitGrantStatement(MySqlParser.GrantStatementContext var1);

    void enterRoleOption(MySqlParser.RoleOptionContext var1);

    void exitRoleOption(MySqlParser.RoleOptionContext var1);

    void enterGrantProxy(MySqlParser.GrantProxyContext var1);

    void exitGrantProxy(MySqlParser.GrantProxyContext var1);

    void enterRenameUser(MySqlParser.RenameUserContext var1);

    void exitRenameUser(MySqlParser.RenameUserContext var1);

    void enterDetailRevoke(MySqlParser.DetailRevokeContext var1);

    void exitDetailRevoke(MySqlParser.DetailRevokeContext var1);

    void enterShortRevoke(MySqlParser.ShortRevokeContext var1);

    void exitShortRevoke(MySqlParser.ShortRevokeContext var1);

    void enterRoleRevoke(MySqlParser.RoleRevokeContext var1);

    void exitRoleRevoke(MySqlParser.RoleRevokeContext var1);

    void enterRevokeProxy(MySqlParser.RevokeProxyContext var1);

    void exitRevokeProxy(MySqlParser.RevokeProxyContext var1);

    void enterSetPasswordStatement(MySqlParser.SetPasswordStatementContext var1);

    void exitSetPasswordStatement(MySqlParser.SetPasswordStatementContext var1);

    void enterUserSpecification(MySqlParser.UserSpecificationContext var1);

    void exitUserSpecification(MySqlParser.UserSpecificationContext var1);

    void enterHashAuthOption(MySqlParser.HashAuthOptionContext var1);

    void exitHashAuthOption(MySqlParser.HashAuthOptionContext var1);

    void enterStringAuthOption(MySqlParser.StringAuthOptionContext var1);

    void exitStringAuthOption(MySqlParser.StringAuthOptionContext var1);

    void enterModuleAuthOption(MySqlParser.ModuleAuthOptionContext var1);

    void exitModuleAuthOption(MySqlParser.ModuleAuthOptionContext var1);

    void enterSimpleAuthOption(MySqlParser.SimpleAuthOptionContext var1);

    void exitSimpleAuthOption(MySqlParser.SimpleAuthOptionContext var1);

    void enterModule(MySqlParser.ModuleContext var1);

    void exitModule(MySqlParser.ModuleContext var1);

    void enterPasswordModuleOption(MySqlParser.PasswordModuleOptionContext var1);

    void exitPasswordModuleOption(MySqlParser.PasswordModuleOptionContext var1);

    void enterTlsOption(MySqlParser.TlsOptionContext var1);

    void exitTlsOption(MySqlParser.TlsOptionContext var1);

    void enterUserResourceOption(MySqlParser.UserResourceOptionContext var1);

    void exitUserResourceOption(MySqlParser.UserResourceOptionContext var1);

    void enterUserPasswordOption(MySqlParser.UserPasswordOptionContext var1);

    void exitUserPasswordOption(MySqlParser.UserPasswordOptionContext var1);

    void enterUserLockOption(MySqlParser.UserLockOptionContext var1);

    void exitUserLockOption(MySqlParser.UserLockOptionContext var1);

    void enterPrivelegeClause(MySqlParser.PrivelegeClauseContext var1);

    void exitPrivelegeClause(MySqlParser.PrivelegeClauseContext var1);

    void enterPrivilege(MySqlParser.PrivilegeContext var1);

    void exitPrivilege(MySqlParser.PrivilegeContext var1);

    void enterCurrentSchemaPriviLevel(MySqlParser.CurrentSchemaPriviLevelContext var1);

    void exitCurrentSchemaPriviLevel(MySqlParser.CurrentSchemaPriviLevelContext var1);

    void enterGlobalPrivLevel(MySqlParser.GlobalPrivLevelContext var1);

    void exitGlobalPrivLevel(MySqlParser.GlobalPrivLevelContext var1);

    void enterDefiniteSchemaPrivLevel(MySqlParser.DefiniteSchemaPrivLevelContext var1);

    void exitDefiniteSchemaPrivLevel(MySqlParser.DefiniteSchemaPrivLevelContext var1);

    void enterDefiniteFullTablePrivLevel(MySqlParser.DefiniteFullTablePrivLevelContext var1);

    void exitDefiniteFullTablePrivLevel(MySqlParser.DefiniteFullTablePrivLevelContext var1);

    void enterDefiniteFullTablePrivLevel2(MySqlParser.DefiniteFullTablePrivLevel2Context var1);

    void exitDefiniteFullTablePrivLevel2(MySqlParser.DefiniteFullTablePrivLevel2Context var1);

    void enterDefiniteTablePrivLevel(MySqlParser.DefiniteTablePrivLevelContext var1);

    void exitDefiniteTablePrivLevel(MySqlParser.DefiniteTablePrivLevelContext var1);

    void enterRenameUserClause(MySqlParser.RenameUserClauseContext var1);

    void exitRenameUserClause(MySqlParser.RenameUserClauseContext var1);

    void enterAnalyzeTable(MySqlParser.AnalyzeTableContext var1);

    void exitAnalyzeTable(MySqlParser.AnalyzeTableContext var1);

    void enterCheckTable(MySqlParser.CheckTableContext var1);

    void exitCheckTable(MySqlParser.CheckTableContext var1);

    void enterChecksumTable(MySqlParser.ChecksumTableContext var1);

    void exitChecksumTable(MySqlParser.ChecksumTableContext var1);

    void enterOptimizeTable(MySqlParser.OptimizeTableContext var1);

    void exitOptimizeTable(MySqlParser.OptimizeTableContext var1);

    void enterRepairTable(MySqlParser.RepairTableContext var1);

    void exitRepairTable(MySqlParser.RepairTableContext var1);

    void enterCheckTableOption(MySqlParser.CheckTableOptionContext var1);

    void exitCheckTableOption(MySqlParser.CheckTableOptionContext var1);

    void enterCreateUdfunction(MySqlParser.CreateUdfunctionContext var1);

    void exitCreateUdfunction(MySqlParser.CreateUdfunctionContext var1);

    void enterInstallPlugin(MySqlParser.InstallPluginContext var1);

    void exitInstallPlugin(MySqlParser.InstallPluginContext var1);

    void enterUninstallPlugin(MySqlParser.UninstallPluginContext var1);

    void exitUninstallPlugin(MySqlParser.UninstallPluginContext var1);

    void enterSetVariable(MySqlParser.SetVariableContext var1);

    void exitSetVariable(MySqlParser.SetVariableContext var1);

    void enterSetCharset(MySqlParser.SetCharsetContext var1);

    void exitSetCharset(MySqlParser.SetCharsetContext var1);

    void enterSetNames(MySqlParser.SetNamesContext var1);

    void exitSetNames(MySqlParser.SetNamesContext var1);

    void enterSetPassword(MySqlParser.SetPasswordContext var1);

    void exitSetPassword(MySqlParser.SetPasswordContext var1);

    void enterSetTransaction(MySqlParser.SetTransactionContext var1);

    void exitSetTransaction(MySqlParser.SetTransactionContext var1);

    void enterSetAutocommit(MySqlParser.SetAutocommitContext var1);

    void exitSetAutocommit(MySqlParser.SetAutocommitContext var1);

    void enterSetNewValueInsideTrigger(MySqlParser.SetNewValueInsideTriggerContext var1);

    void exitSetNewValueInsideTrigger(MySqlParser.SetNewValueInsideTriggerContext var1);

    void enterShowMasterLogs(MySqlParser.ShowMasterLogsContext var1);

    void exitShowMasterLogs(MySqlParser.ShowMasterLogsContext var1);

    void enterShowLogEvents(MySqlParser.ShowLogEventsContext var1);

    void exitShowLogEvents(MySqlParser.ShowLogEventsContext var1);

    void enterShowObjectFilter(MySqlParser.ShowObjectFilterContext var1);

    void exitShowObjectFilter(MySqlParser.ShowObjectFilterContext var1);

    void enterShowColumns(MySqlParser.ShowColumnsContext var1);

    void exitShowColumns(MySqlParser.ShowColumnsContext var1);

    void enterShowCreateDb(MySqlParser.ShowCreateDbContext var1);

    void exitShowCreateDb(MySqlParser.ShowCreateDbContext var1);

    void enterShowCreateFullIdObject(MySqlParser.ShowCreateFullIdObjectContext var1);

    void exitShowCreateFullIdObject(MySqlParser.ShowCreateFullIdObjectContext var1);

    void enterShowCreateUser(MySqlParser.ShowCreateUserContext var1);

    void exitShowCreateUser(MySqlParser.ShowCreateUserContext var1);

    void enterShowEngine(MySqlParser.ShowEngineContext var1);

    void exitShowEngine(MySqlParser.ShowEngineContext var1);

    void enterShowGlobalInfo(MySqlParser.ShowGlobalInfoContext var1);

    void exitShowGlobalInfo(MySqlParser.ShowGlobalInfoContext var1);

    void enterShowErrors(MySqlParser.ShowErrorsContext var1);

    void exitShowErrors(MySqlParser.ShowErrorsContext var1);

    void enterShowCountErrors(MySqlParser.ShowCountErrorsContext var1);

    void exitShowCountErrors(MySqlParser.ShowCountErrorsContext var1);

    void enterShowSchemaFilter(MySqlParser.ShowSchemaFilterContext var1);

    void exitShowSchemaFilter(MySqlParser.ShowSchemaFilterContext var1);

    void enterShowRoutine(MySqlParser.ShowRoutineContext var1);

    void exitShowRoutine(MySqlParser.ShowRoutineContext var1);

    void enterShowGrants(MySqlParser.ShowGrantsContext var1);

    void exitShowGrants(MySqlParser.ShowGrantsContext var1);

    void enterShowIndexes(MySqlParser.ShowIndexesContext var1);

    void exitShowIndexes(MySqlParser.ShowIndexesContext var1);

    void enterShowOpenTables(MySqlParser.ShowOpenTablesContext var1);

    void exitShowOpenTables(MySqlParser.ShowOpenTablesContext var1);

    void enterShowProfile(MySqlParser.ShowProfileContext var1);

    void exitShowProfile(MySqlParser.ShowProfileContext var1);

    void enterShowSlaveStatus(MySqlParser.ShowSlaveStatusContext var1);

    void exitShowSlaveStatus(MySqlParser.ShowSlaveStatusContext var1);

    void enterShowUserstatPlugin(MySqlParser.ShowUserstatPluginContext var1);

    void exitShowUserstatPlugin(MySqlParser.ShowUserstatPluginContext var1);

    void enterVariableClause(MySqlParser.VariableClauseContext var1);

    void exitVariableClause(MySqlParser.VariableClauseContext var1);

    void enterShowCommonEntity(MySqlParser.ShowCommonEntityContext var1);

    void exitShowCommonEntity(MySqlParser.ShowCommonEntityContext var1);

    void enterShowFilter(MySqlParser.ShowFilterContext var1);

    void exitShowFilter(MySqlParser.ShowFilterContext var1);

    void enterShowGlobalInfoClause(MySqlParser.ShowGlobalInfoClauseContext var1);

    void exitShowGlobalInfoClause(MySqlParser.ShowGlobalInfoClauseContext var1);

    void enterShowSchemaEntity(MySqlParser.ShowSchemaEntityContext var1);

    void exitShowSchemaEntity(MySqlParser.ShowSchemaEntityContext var1);

    void enterShowProfileType(MySqlParser.ShowProfileTypeContext var1);

    void exitShowProfileType(MySqlParser.ShowProfileTypeContext var1);

    void enterBinlogStatement(MySqlParser.BinlogStatementContext var1);

    void exitBinlogStatement(MySqlParser.BinlogStatementContext var1);

    void enterCacheIndexStatement(MySqlParser.CacheIndexStatementContext var1);

    void exitCacheIndexStatement(MySqlParser.CacheIndexStatementContext var1);

    void enterFlushStatement(MySqlParser.FlushStatementContext var1);

    void exitFlushStatement(MySqlParser.FlushStatementContext var1);

    void enterKillStatement(MySqlParser.KillStatementContext var1);

    void exitKillStatement(MySqlParser.KillStatementContext var1);

    void enterLoadIndexIntoCache(MySqlParser.LoadIndexIntoCacheContext var1);

    void exitLoadIndexIntoCache(MySqlParser.LoadIndexIntoCacheContext var1);

    void enterResetStatement(MySqlParser.ResetStatementContext var1);

    void exitResetStatement(MySqlParser.ResetStatementContext var1);

    void enterShutdownStatement(MySqlParser.ShutdownStatementContext var1);

    void exitShutdownStatement(MySqlParser.ShutdownStatementContext var1);

    void enterTableIndexes(MySqlParser.TableIndexesContext var1);

    void exitTableIndexes(MySqlParser.TableIndexesContext var1);

    void enterSimpleFlushOption(MySqlParser.SimpleFlushOptionContext var1);

    void exitSimpleFlushOption(MySqlParser.SimpleFlushOptionContext var1);

    void enterChannelFlushOption(MySqlParser.ChannelFlushOptionContext var1);

    void exitChannelFlushOption(MySqlParser.ChannelFlushOptionContext var1);

    void enterTableFlushOption(MySqlParser.TableFlushOptionContext var1);

    void exitTableFlushOption(MySqlParser.TableFlushOptionContext var1);

    void enterFlushTableOption(MySqlParser.FlushTableOptionContext var1);

    void exitFlushTableOption(MySqlParser.FlushTableOptionContext var1);

    void enterLoadedTableIndexes(MySqlParser.LoadedTableIndexesContext var1);

    void exitLoadedTableIndexes(MySqlParser.LoadedTableIndexesContext var1);

    void enterSimpleDescribeStatement(MySqlParser.SimpleDescribeStatementContext var1);

    void exitSimpleDescribeStatement(MySqlParser.SimpleDescribeStatementContext var1);

    void enterFullDescribeStatement(MySqlParser.FullDescribeStatementContext var1);

    void exitFullDescribeStatement(MySqlParser.FullDescribeStatementContext var1);

    void enterHelpStatement(MySqlParser.HelpStatementContext var1);

    void exitHelpStatement(MySqlParser.HelpStatementContext var1);

    void enterUseStatement(MySqlParser.UseStatementContext var1);

    void exitUseStatement(MySqlParser.UseStatementContext var1);

    void enterSignalStatement(MySqlParser.SignalStatementContext var1);

    void exitSignalStatement(MySqlParser.SignalStatementContext var1);

    void enterResignalStatement(MySqlParser.ResignalStatementContext var1);

    void exitResignalStatement(MySqlParser.ResignalStatementContext var1);

    void enterSignalConditionInformation(MySqlParser.SignalConditionInformationContext var1);

    void exitSignalConditionInformation(MySqlParser.SignalConditionInformationContext var1);

    void enterDiagnosticsStatement(MySqlParser.DiagnosticsStatementContext var1);

    void exitDiagnosticsStatement(MySqlParser.DiagnosticsStatementContext var1);

    void enterDiagnosticsConditionInformationName(MySqlParser.DiagnosticsConditionInformationNameContext var1);

    void exitDiagnosticsConditionInformationName(MySqlParser.DiagnosticsConditionInformationNameContext var1);

    void enterDescribeStatements(MySqlParser.DescribeStatementsContext var1);

    void exitDescribeStatements(MySqlParser.DescribeStatementsContext var1);

    void enterDescribeConnection(MySqlParser.DescribeConnectionContext var1);

    void exitDescribeConnection(MySqlParser.DescribeConnectionContext var1);

    void enterFullId(MySqlParser.FullIdContext var1);

    void exitFullId(MySqlParser.FullIdContext var1);

    void enterTableName(MySqlParser.TableNameContext var1);

    void exitTableName(MySqlParser.TableNameContext var1);

    void enterRoleName(MySqlParser.RoleNameContext var1);

    void exitRoleName(MySqlParser.RoleNameContext var1);

    void enterFullColumnName(MySqlParser.FullColumnNameContext var1);

    void exitFullColumnName(MySqlParser.FullColumnNameContext var1);

    void enterIndexColumnName(MySqlParser.IndexColumnNameContext var1);

    void exitIndexColumnName(MySqlParser.IndexColumnNameContext var1);

    void enterUserName(MySqlParser.UserNameContext var1);

    void exitUserName(MySqlParser.UserNameContext var1);

    void enterMysqlVariable(MySqlParser.MysqlVariableContext var1);

    void exitMysqlVariable(MySqlParser.MysqlVariableContext var1);

    void enterCharsetName(MySqlParser.CharsetNameContext var1);

    void exitCharsetName(MySqlParser.CharsetNameContext var1);

    void enterCollationName(MySqlParser.CollationNameContext var1);

    void exitCollationName(MySqlParser.CollationNameContext var1);

    void enterEngineName(MySqlParser.EngineNameContext var1);

    void exitEngineName(MySqlParser.EngineNameContext var1);

    void enterEngineNameBase(MySqlParser.EngineNameBaseContext var1);

    void exitEngineNameBase(MySqlParser.EngineNameBaseContext var1);

    void enterEncryptedLiteral(MySqlParser.EncryptedLiteralContext var1);

    void exitEncryptedLiteral(MySqlParser.EncryptedLiteralContext var1);

    void enterUuidSet(MySqlParser.UuidSetContext var1);

    void exitUuidSet(MySqlParser.UuidSetContext var1);

    void enterXid(MySqlParser.XidContext var1);

    void exitXid(MySqlParser.XidContext var1);

    void enterXuidStringId(MySqlParser.XuidStringIdContext var1);

    void exitXuidStringId(MySqlParser.XuidStringIdContext var1);

    void enterAuthPlugin(MySqlParser.AuthPluginContext var1);

    void exitAuthPlugin(MySqlParser.AuthPluginContext var1);

    void enterUid(MySqlParser.UidContext var1);

    void exitUid(MySqlParser.UidContext var1);

    void enterSimpleId(MySqlParser.SimpleIdContext var1);

    void exitSimpleId(MySqlParser.SimpleIdContext var1);

    void enterDottedId(MySqlParser.DottedIdContext var1);

    void exitDottedId(MySqlParser.DottedIdContext var1);

    void enterDecimalLiteral(MySqlParser.DecimalLiteralContext var1);

    void exitDecimalLiteral(MySqlParser.DecimalLiteralContext var1);

    void enterFileSizeLiteral(MySqlParser.FileSizeLiteralContext var1);

    void exitFileSizeLiteral(MySqlParser.FileSizeLiteralContext var1);

    void enterStringLiteral(MySqlParser.StringLiteralContext var1);

    void exitStringLiteral(MySqlParser.StringLiteralContext var1);

    void enterBooleanLiteral(MySqlParser.BooleanLiteralContext var1);

    void exitBooleanLiteral(MySqlParser.BooleanLiteralContext var1);

    void enterHexadecimalLiteral(MySqlParser.HexadecimalLiteralContext var1);

    void exitHexadecimalLiteral(MySqlParser.HexadecimalLiteralContext var1);

    void enterNullNotnull(MySqlParser.NullNotnullContext var1);

    void exitNullNotnull(MySqlParser.NullNotnullContext var1);

    void enterConstant(MySqlParser.ConstantContext var1);

    void exitConstant(MySqlParser.ConstantContext var1);

    void enterStringDataType(MySqlParser.StringDataTypeContext var1);

    void exitStringDataType(MySqlParser.StringDataTypeContext var1);

    void enterNationalVaryingStringDataType(MySqlParser.NationalVaryingStringDataTypeContext var1);

    void exitNationalVaryingStringDataType(MySqlParser.NationalVaryingStringDataTypeContext var1);

    void enterNationalStringDataType(MySqlParser.NationalStringDataTypeContext var1);

    void exitNationalStringDataType(MySqlParser.NationalStringDataTypeContext var1);

    void enterDimensionDataType(MySqlParser.DimensionDataTypeContext var1);

    void exitDimensionDataType(MySqlParser.DimensionDataTypeContext var1);

    void enterSimpleDataType(MySqlParser.SimpleDataTypeContext var1);

    void exitSimpleDataType(MySqlParser.SimpleDataTypeContext var1);

    void enterCollectionDataType(MySqlParser.CollectionDataTypeContext var1);

    void exitCollectionDataType(MySqlParser.CollectionDataTypeContext var1);

    void enterSpatialDataType(MySqlParser.SpatialDataTypeContext var1);

    void exitSpatialDataType(MySqlParser.SpatialDataTypeContext var1);

    void enterLongVarcharDataType(MySqlParser.LongVarcharDataTypeContext var1);

    void exitLongVarcharDataType(MySqlParser.LongVarcharDataTypeContext var1);

    void enterLongVarbinaryDataType(MySqlParser.LongVarbinaryDataTypeContext var1);

    void exitLongVarbinaryDataType(MySqlParser.LongVarbinaryDataTypeContext var1);

    void enterUuidDataType(MySqlParser.UuidDataTypeContext var1);

    void exitUuidDataType(MySqlParser.UuidDataTypeContext var1);

    void enterCollectionOptions(MySqlParser.CollectionOptionsContext var1);

    void exitCollectionOptions(MySqlParser.CollectionOptionsContext var1);

    void enterCollectionOption(MySqlParser.CollectionOptionContext var1);

    void exitCollectionOption(MySqlParser.CollectionOptionContext var1);

    void enterConvertedDataType(MySqlParser.ConvertedDataTypeContext var1);

    void exitConvertedDataType(MySqlParser.ConvertedDataTypeContext var1);

    void enterLengthOneDimension(MySqlParser.LengthOneDimensionContext var1);

    void exitLengthOneDimension(MySqlParser.LengthOneDimensionContext var1);

    void enterLengthTwoDimension(MySqlParser.LengthTwoDimensionContext var1);

    void exitLengthTwoDimension(MySqlParser.LengthTwoDimensionContext var1);

    void enterLengthTwoOptionalDimension(MySqlParser.LengthTwoOptionalDimensionContext var1);

    void exitLengthTwoOptionalDimension(MySqlParser.LengthTwoOptionalDimensionContext var1);

    void enterUidList(MySqlParser.UidListContext var1);

    void exitUidList(MySqlParser.UidListContext var1);

    void enterFullColumnNameList(MySqlParser.FullColumnNameListContext var1);

    void exitFullColumnNameList(MySqlParser.FullColumnNameListContext var1);

    void enterTables(MySqlParser.TablesContext var1);

    void exitTables(MySqlParser.TablesContext var1);

    void enterIndexColumnNames(MySqlParser.IndexColumnNamesContext var1);

    void exitIndexColumnNames(MySqlParser.IndexColumnNamesContext var1);

    void enterExpressions(MySqlParser.ExpressionsContext var1);

    void exitExpressions(MySqlParser.ExpressionsContext var1);

    void enterExpressionsWithDefaults(MySqlParser.ExpressionsWithDefaultsContext var1);

    void exitExpressionsWithDefaults(MySqlParser.ExpressionsWithDefaultsContext var1);

    void enterConstants(MySqlParser.ConstantsContext var1);

    void exitConstants(MySqlParser.ConstantsContext var1);

    void enterSimpleStrings(MySqlParser.SimpleStringsContext var1);

    void exitSimpleStrings(MySqlParser.SimpleStringsContext var1);

    void enterUserVariables(MySqlParser.UserVariablesContext var1);

    void exitUserVariables(MySqlParser.UserVariablesContext var1);

    void enterDefaultValue(MySqlParser.DefaultValueContext var1);

    void exitDefaultValue(MySqlParser.DefaultValueContext var1);

    void enterCurrentTimestamp(MySqlParser.CurrentTimestampContext var1);

    void exitCurrentTimestamp(MySqlParser.CurrentTimestampContext var1);

    void enterExpressionOrDefault(MySqlParser.ExpressionOrDefaultContext var1);

    void exitExpressionOrDefault(MySqlParser.ExpressionOrDefaultContext var1);

    void enterIfExists(MySqlParser.IfExistsContext var1);

    void exitIfExists(MySqlParser.IfExistsContext var1);

    void enterIfNotExists(MySqlParser.IfNotExistsContext var1);

    void exitIfNotExists(MySqlParser.IfNotExistsContext var1);

    void enterOrReplace(MySqlParser.OrReplaceContext var1);

    void exitOrReplace(MySqlParser.OrReplaceContext var1);

    void enterWaitNowaitClause(MySqlParser.WaitNowaitClauseContext var1);

    void exitWaitNowaitClause(MySqlParser.WaitNowaitClauseContext var1);

    void enterLockOption(MySqlParser.LockOptionContext var1);

    void exitLockOption(MySqlParser.LockOptionContext var1);

    void enterSpecificFunctionCall(MySqlParser.SpecificFunctionCallContext var1);

    void exitSpecificFunctionCall(MySqlParser.SpecificFunctionCallContext var1);

    void enterAggregateFunctionCall(MySqlParser.AggregateFunctionCallContext var1);

    void exitAggregateFunctionCall(MySqlParser.AggregateFunctionCallContext var1);

    void enterNonAggregateFunctionCall(MySqlParser.NonAggregateFunctionCallContext var1);

    void exitNonAggregateFunctionCall(MySqlParser.NonAggregateFunctionCallContext var1);

    void enterScalarFunctionCall(MySqlParser.ScalarFunctionCallContext var1);

    void exitScalarFunctionCall(MySqlParser.ScalarFunctionCallContext var1);

    void enterUdfFunctionCall(MySqlParser.UdfFunctionCallContext var1);

    void exitUdfFunctionCall(MySqlParser.UdfFunctionCallContext var1);

    void enterPasswordFunctionCall(MySqlParser.PasswordFunctionCallContext var1);

    void exitPasswordFunctionCall(MySqlParser.PasswordFunctionCallContext var1);

    void enterSimpleFunctionCall(MySqlParser.SimpleFunctionCallContext var1);

    void exitSimpleFunctionCall(MySqlParser.SimpleFunctionCallContext var1);

    void enterDataTypeFunctionCall(MySqlParser.DataTypeFunctionCallContext var1);

    void exitDataTypeFunctionCall(MySqlParser.DataTypeFunctionCallContext var1);

    void enterValuesFunctionCall(MySqlParser.ValuesFunctionCallContext var1);

    void exitValuesFunctionCall(MySqlParser.ValuesFunctionCallContext var1);

    void enterCaseExpressionFunctionCall(MySqlParser.CaseExpressionFunctionCallContext var1);

    void exitCaseExpressionFunctionCall(MySqlParser.CaseExpressionFunctionCallContext var1);

    void enterCaseFunctionCall(MySqlParser.CaseFunctionCallContext var1);

    void exitCaseFunctionCall(MySqlParser.CaseFunctionCallContext var1);

    void enterCharFunctionCall(MySqlParser.CharFunctionCallContext var1);

    void exitCharFunctionCall(MySqlParser.CharFunctionCallContext var1);

    void enterPositionFunctionCall(MySqlParser.PositionFunctionCallContext var1);

    void exitPositionFunctionCall(MySqlParser.PositionFunctionCallContext var1);

    void enterSubstrFunctionCall(MySqlParser.SubstrFunctionCallContext var1);

    void exitSubstrFunctionCall(MySqlParser.SubstrFunctionCallContext var1);

    void enterTrimFunctionCall(MySqlParser.TrimFunctionCallContext var1);

    void exitTrimFunctionCall(MySqlParser.TrimFunctionCallContext var1);

    void enterWeightFunctionCall(MySqlParser.WeightFunctionCallContext var1);

    void exitWeightFunctionCall(MySqlParser.WeightFunctionCallContext var1);

    void enterExtractFunctionCall(MySqlParser.ExtractFunctionCallContext var1);

    void exitExtractFunctionCall(MySqlParser.ExtractFunctionCallContext var1);

    void enterGetFormatFunctionCall(MySqlParser.GetFormatFunctionCallContext var1);

    void exitGetFormatFunctionCall(MySqlParser.GetFormatFunctionCallContext var1);

    void enterJsonValueFunctionCall(MySqlParser.JsonValueFunctionCallContext var1);

    void exitJsonValueFunctionCall(MySqlParser.JsonValueFunctionCallContext var1);

    void enterCaseFuncAlternative(MySqlParser.CaseFuncAlternativeContext var1);

    void exitCaseFuncAlternative(MySqlParser.CaseFuncAlternativeContext var1);

    void enterLevelWeightList(MySqlParser.LevelWeightListContext var1);

    void exitLevelWeightList(MySqlParser.LevelWeightListContext var1);

    void enterLevelWeightRange(MySqlParser.LevelWeightRangeContext var1);

    void exitLevelWeightRange(MySqlParser.LevelWeightRangeContext var1);

    void enterLevelInWeightListElement(MySqlParser.LevelInWeightListElementContext var1);

    void exitLevelInWeightListElement(MySqlParser.LevelInWeightListElementContext var1);

    void enterAggregateWindowedFunction(MySqlParser.AggregateWindowedFunctionContext var1);

    void exitAggregateWindowedFunction(MySqlParser.AggregateWindowedFunctionContext var1);

    void enterNonAggregateWindowedFunction(MySqlParser.NonAggregateWindowedFunctionContext var1);

    void exitNonAggregateWindowedFunction(MySqlParser.NonAggregateWindowedFunctionContext var1);

    void enterOverClause(MySqlParser.OverClauseContext var1);

    void exitOverClause(MySqlParser.OverClauseContext var1);

    void enterWindowSpec(MySqlParser.WindowSpecContext var1);

    void exitWindowSpec(MySqlParser.WindowSpecContext var1);

    void enterWindowName(MySqlParser.WindowNameContext var1);

    void exitWindowName(MySqlParser.WindowNameContext var1);

    void enterFrameClause(MySqlParser.FrameClauseContext var1);

    void exitFrameClause(MySqlParser.FrameClauseContext var1);

    void enterFrameUnits(MySqlParser.FrameUnitsContext var1);

    void exitFrameUnits(MySqlParser.FrameUnitsContext var1);

    void enterFrameExtent(MySqlParser.FrameExtentContext var1);

    void exitFrameExtent(MySqlParser.FrameExtentContext var1);

    void enterFrameBetween(MySqlParser.FrameBetweenContext var1);

    void exitFrameBetween(MySqlParser.FrameBetweenContext var1);

    void enterFrameRange(MySqlParser.FrameRangeContext var1);

    void exitFrameRange(MySqlParser.FrameRangeContext var1);

    void enterPartitionClause(MySqlParser.PartitionClauseContext var1);

    void exitPartitionClause(MySqlParser.PartitionClauseContext var1);

    void enterScalarFunctionName(MySqlParser.ScalarFunctionNameContext var1);

    void exitScalarFunctionName(MySqlParser.ScalarFunctionNameContext var1);

    void enterPasswordFunctionClause(MySqlParser.PasswordFunctionClauseContext var1);

    void exitPasswordFunctionClause(MySqlParser.PasswordFunctionClauseContext var1);

    void enterFunctionArgs(MySqlParser.FunctionArgsContext var1);

    void exitFunctionArgs(MySqlParser.FunctionArgsContext var1);

    void enterFunctionArg(MySqlParser.FunctionArgContext var1);

    void exitFunctionArg(MySqlParser.FunctionArgContext var1);

    void enterIsExpression(MySqlParser.IsExpressionContext var1);

    void exitIsExpression(MySqlParser.IsExpressionContext var1);

    void enterNotExpression(MySqlParser.NotExpressionContext var1);

    void exitNotExpression(MySqlParser.NotExpressionContext var1);

    void enterLogicalExpression(MySqlParser.LogicalExpressionContext var1);

    void exitLogicalExpression(MySqlParser.LogicalExpressionContext var1);

    void enterPredicateExpression(MySqlParser.PredicateExpressionContext var1);

    void exitPredicateExpression(MySqlParser.PredicateExpressionContext var1);

    void enterSoundsLikePredicate(MySqlParser.SoundsLikePredicateContext var1);

    void exitSoundsLikePredicate(MySqlParser.SoundsLikePredicateContext var1);

    void enterExpressionAtomPredicate(MySqlParser.ExpressionAtomPredicateContext var1);

    void exitExpressionAtomPredicate(MySqlParser.ExpressionAtomPredicateContext var1);

    void enterSubqueryComparisonPredicate(MySqlParser.SubqueryComparisonPredicateContext var1);

    void exitSubqueryComparisonPredicate(MySqlParser.SubqueryComparisonPredicateContext var1);

    void enterJsonMemberOfPredicate(MySqlParser.JsonMemberOfPredicateContext var1);

    void exitJsonMemberOfPredicate(MySqlParser.JsonMemberOfPredicateContext var1);

    void enterBinaryComparisonPredicate(MySqlParser.BinaryComparisonPredicateContext var1);

    void exitBinaryComparisonPredicate(MySqlParser.BinaryComparisonPredicateContext var1);

    void enterInPredicate(MySqlParser.InPredicateContext var1);

    void exitInPredicate(MySqlParser.InPredicateContext var1);

    void enterBetweenPredicate(MySqlParser.BetweenPredicateContext var1);

    void exitBetweenPredicate(MySqlParser.BetweenPredicateContext var1);

    void enterIsNullPredicate(MySqlParser.IsNullPredicateContext var1);

    void exitIsNullPredicate(MySqlParser.IsNullPredicateContext var1);

    void enterLikePredicate(MySqlParser.LikePredicateContext var1);

    void exitLikePredicate(MySqlParser.LikePredicateContext var1);

    void enterRegexpPredicate(MySqlParser.RegexpPredicateContext var1);

    void exitRegexpPredicate(MySqlParser.RegexpPredicateContext var1);

    void enterUnaryExpressionAtom(MySqlParser.UnaryExpressionAtomContext var1);

    void exitUnaryExpressionAtom(MySqlParser.UnaryExpressionAtomContext var1);

    void enterCollateExpressionAtom(MySqlParser.CollateExpressionAtomContext var1);

    void exitCollateExpressionAtom(MySqlParser.CollateExpressionAtomContext var1);

    void enterMysqlVariableExpressionAtom(MySqlParser.MysqlVariableExpressionAtomContext var1);

    void exitMysqlVariableExpressionAtom(MySqlParser.MysqlVariableExpressionAtomContext var1);

    void enterNestedExpressionAtom(MySqlParser.NestedExpressionAtomContext var1);

    void exitNestedExpressionAtom(MySqlParser.NestedExpressionAtomContext var1);

    void enterNestedRowExpressionAtom(MySqlParser.NestedRowExpressionAtomContext var1);

    void exitNestedRowExpressionAtom(MySqlParser.NestedRowExpressionAtomContext var1);

    void enterMathExpressionAtom(MySqlParser.MathExpressionAtomContext var1);

    void exitMathExpressionAtom(MySqlParser.MathExpressionAtomContext var1);

    void enterExistsExpressionAtom(MySqlParser.ExistsExpressionAtomContext var1);

    void exitExistsExpressionAtom(MySqlParser.ExistsExpressionAtomContext var1);

    void enterIntervalExpressionAtom(MySqlParser.IntervalExpressionAtomContext var1);

    void exitIntervalExpressionAtom(MySqlParser.IntervalExpressionAtomContext var1);

    void enterJsonExpressionAtom(MySqlParser.JsonExpressionAtomContext var1);

    void exitJsonExpressionAtom(MySqlParser.JsonExpressionAtomContext var1);

    void enterSubqueryExpressionAtom(MySqlParser.SubqueryExpressionAtomContext var1);

    void exitSubqueryExpressionAtom(MySqlParser.SubqueryExpressionAtomContext var1);

    void enterConstantExpressionAtom(MySqlParser.ConstantExpressionAtomContext var1);

    void exitConstantExpressionAtom(MySqlParser.ConstantExpressionAtomContext var1);

    void enterFunctionCallExpressionAtom(MySqlParser.FunctionCallExpressionAtomContext var1);

    void exitFunctionCallExpressionAtom(MySqlParser.FunctionCallExpressionAtomContext var1);

    void enterBinaryExpressionAtom(MySqlParser.BinaryExpressionAtomContext var1);

    void exitBinaryExpressionAtom(MySqlParser.BinaryExpressionAtomContext var1);

    void enterFullColumnNameExpressionAtom(MySqlParser.FullColumnNameExpressionAtomContext var1);

    void exitFullColumnNameExpressionAtom(MySqlParser.FullColumnNameExpressionAtomContext var1);

    void enterBitExpressionAtom(MySqlParser.BitExpressionAtomContext var1);

    void exitBitExpressionAtom(MySqlParser.BitExpressionAtomContext var1);

    void enterUnaryOperator(MySqlParser.UnaryOperatorContext var1);

    void exitUnaryOperator(MySqlParser.UnaryOperatorContext var1);

    void enterComparisonOperator(MySqlParser.ComparisonOperatorContext var1);

    void exitComparisonOperator(MySqlParser.ComparisonOperatorContext var1);

    void enterLogicalOperator(MySqlParser.LogicalOperatorContext var1);

    void exitLogicalOperator(MySqlParser.LogicalOperatorContext var1);

    void enterBitOperator(MySqlParser.BitOperatorContext var1);

    void exitBitOperator(MySqlParser.BitOperatorContext var1);

    void enterMathOperator(MySqlParser.MathOperatorContext var1);

    void exitMathOperator(MySqlParser.MathOperatorContext var1);

    void enterJsonOperator(MySqlParser.JsonOperatorContext var1);

    void exitJsonOperator(MySqlParser.JsonOperatorContext var1);

    void enterCharsetNameBase(MySqlParser.CharsetNameBaseContext var1);

    void exitCharsetNameBase(MySqlParser.CharsetNameBaseContext var1);

    void enterTransactionLevelBase(MySqlParser.TransactionLevelBaseContext var1);

    void exitTransactionLevelBase(MySqlParser.TransactionLevelBaseContext var1);

    void enterPrivilegesBase(MySqlParser.PrivilegesBaseContext var1);

    void exitPrivilegesBase(MySqlParser.PrivilegesBaseContext var1);

    void enterIntervalTypeBase(MySqlParser.IntervalTypeBaseContext var1);

    void exitIntervalTypeBase(MySqlParser.IntervalTypeBaseContext var1);

    void enterDataTypeBase(MySqlParser.DataTypeBaseContext var1);

    void exitDataTypeBase(MySqlParser.DataTypeBaseContext var1);

    void enterKeywordsCanBeId(MySqlParser.KeywordsCanBeIdContext var1);

    void exitKeywordsCanBeId(MySqlParser.KeywordsCanBeIdContext var1);

    void enterFunctionNameBase(MySqlParser.FunctionNameBaseContext var1);

    void exitFunctionNameBase(MySqlParser.FunctionNameBaseContext var1);
}
