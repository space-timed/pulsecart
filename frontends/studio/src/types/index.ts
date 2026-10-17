export interface ServiceNode {
  id: string;
  name: string;
  port: number;
  tier: 'gateway' | 'core' | 'checkout' | 'async' | 'cqrs' | 'infra';
  protocol: 'REST' | 'Kafka' | 'Both';
  database: string;
  status: 'HEALTHY' | 'DEGRADED' | 'DOWN';
  patterns: string[];
  publishedEvents: string[];
  consumedEvents: string[];
  endpoints: string[];
  schemaOverview: string;
}

export interface TraceSpan {
  id: string;
  serviceId: string;
  serviceName: string;
  operation: string;
  durationMs: number;
  status: 'SUCCESS' | 'ERROR' | 'COMPENSATED';
  timestamp: string;
  correlationId: string;
  details: string;
}

export interface ArchitecturePattern {
  id: string;
  name: string;
  category: 'Distributed Transactions' | 'Event-Driven & Messaging' | 'Resilience & Fault Tolerance' | 'Concurrency' | 'CQRS & Read Models';
  services: string[];
  summary: string;
  problemSolved: string;
  implementationDetails: string;
  codeSnippet: string;
}

export interface KafkaTopicMetadata {
  name: string;
  partitions: number;
  consumerGroups: string[];
  isDlq: boolean;
  retentionHours: number;
  description: string;
}

export interface DatabaseMetadata {
  serviceId: string;
  dbName: string;
  schema: string;
  engine: string;
  primaryTables: string[];
  isolationGuarantee: string;
}
